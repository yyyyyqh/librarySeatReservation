<template>
  <div class="login-container">
    <div class="login-box">
      <h2 class="title">图书馆座位预约系统</h2>
      <el-form :model="form" :rules="rules" ref="formRef" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入账号/学号">
            <template #prefix
              ><el-icon><User /></el-icon
            ></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
          >
            <template #prefix
              ><el-icon><Lock /></el-icon
            ></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            立即登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="tips">
        <p>学生账号: test_student / 123456</p>
        <p>管理员账号: admin / 123456</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "../stores/user";
import { ElMessage } from "element-plus";

const router = useRouter();
const userStore = useUserStore();

const formRef = ref(null);
const loading = ref(false);

const form = reactive({
  username: "",
  password: "",
});

const rules = {
  username: [{ required: true, message: "请输入账号", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
};

const handleLogin = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const user = await userStore.login(form);
        ElMessage.success(`欢迎回来，${user.realName}`);

        // 关键逻辑：根据身份跳转不同页面
        // 0:学生, 1:教师 -> 跳转学生端
        // 2:管理员 -> 跳转管理端
        if (user.identityType === 2) {
          router.push("/admin/dashboard");
        } else {
          router.push("/student/home");
        }
      } catch (error) {
        // 错误已经在 request.js 处理过了，这里不用处理
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
}
.login-box {
  width: 400px;
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
.login-btn {
  width: 100%;
}
.tips {
  margin-top: 20px;
  font-size: 12px;
  color: #999;
  text-align: center;
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
}
</style>
