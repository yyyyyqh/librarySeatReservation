<template>
  <div class="register-container">
    <div class="register-box">
      <h2 class="title">新用户注册</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="设置登录账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="设置登录密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPass">
          <el-input
            v-model="form.confirmPass"
            type="password"
            placeholder="再次输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="学号" prop="campusId">
          <el-input v-model="form.campusId" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="选填" />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="w-100"
            :loading="loading"
            @click="handleRegister"
          >
            立即注册
          </el-button>
        </el-form-item>
        <div class="link-area">
          <el-button link type="primary" @click="router.push('/login')"
            >已有账号？去登录</el-button
          >
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import request from "@/utils/request";
import { ElMessage } from "element-plus";

const router = useRouter();
const formRef = ref(null);
const loading = ref(false);

const form = reactive({
  username: "",
  password: "",
  confirmPass: "",
  realName: "",
  campusId: "",
  phone: "",
});

// 密码一致性校验
const validatePass2 = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error("两次输入密码不一致!"));
  } else {
    callback();
  }
};

const rules = {
  username: [{ required: true, message: "请输入账号", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
  confirmPass: [{ required: true, validator: validatePass2, trigger: "blur" }],
  realName: [{ required: true, message: "请输入姓名", trigger: "blur" }],
  campusId: [{ required: true, message: "请输入学号", trigger: "blur" }],
};

const handleRegister = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        await request.post("/api/user/register", {
          username: form.username,
          password: form.password,
          realName: form.realName,
          campusId: form.campusId,
          phone: form.phone,
        });
        ElMessage.success("注册成功，请登录");
        router.push("/login");
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
}
.register-box {
  width: 450px;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
.title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}
.w-100 {
  width: 100%;
}
.link-area {
  text-align: center;
  margin-top: 10px;
}
</style>
