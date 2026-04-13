import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import ElementUI from "element-ui";
import "element-ui/lib/theme-chalk/index.css";
import '../src/assets/init.css'
import '../src/assets/fonts/fonts.css'
import moment from 'moment'
import 'moment/locale/zh-cn'
// main.js
import '@wangeditor/editor/dist/css/style.css'
import VueCropper from 'vue-cropper'

// 设置moment语言为中文
moment.locale('zh-cn')

Vue.use(ElementUI);
Vue.config.productionTip = false;

Vue.prototype.HOST = "/api"

Vue.use(VueCropper)

new Vue({
  router,
  store,
  render: h => h(App),
}).$mount("#app");
