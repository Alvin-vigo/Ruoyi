'use strict'
const path = require('path')

function resolve(dir) {
  return path.join(__dirname, dir)
}

module.exports = {
  publicPath: process.env.VUE_APP_PUBLIC_PATH || '/',
  outputDir: 'dist',
  assetsDir: 'static',
  // 关闭开发期 eslint 校验，保证骨架能直接启动（如需规范检查再开启）
  lintOnSave: false,
  productionSourceMap: false,
  devServer: {
    host: '0.0.0.0',
    port: 8081,
    open: true,
    overlay: {
      warnings: false,
      errors: true
    },
    proxy: {
      // 登录鉴权三件套（/login /logout /getInfo 无业务前缀，必须单独转发，否则 404）
      '/login': { target: 'http://localhost:8080', changeOrigin: true },
      '/logout': { target: 'http://localhost:8080', changeOrigin: true },
      '/getInfo': { target: 'http://localhost:8080', changeOrigin: true },
      // 后端若依服务（默认 8080），前端请求按路径前缀转发
      '/medical': { target: 'http://localhost:8080', changeOrigin: true },
      '/monitor': { target: 'http://localhost:8080', changeOrigin: true },
      '/system': { target: 'http://localhost:8080', changeOrigin: true },
      '/tool': { target: 'http://localhost:8080', changeOrigin: true },
      '/gen': { target: 'http://localhost:8080', changeOrigin: true },
      // AI 微服务静态资源：胸片原图由 FastAPI 保存在 ruoyi-ai/data/uploads 下，
      // 接口返回的 img_url 形如 /ai-uploads/xxx.png，必须转发到 8000 端口才能显示。
      '/ai-uploads': { target: 'http://127.0.0.1:8000', changeOrigin: true }
    }
  },
  configureWebpack: {
    resolve: {
      alias: {
        '@': resolve('src')
      }
    },
    optimization: {
      // 把体积大、改动频率低的第三方库拆成独立 chunk。
      // 收益：业务代码更新时这些 chunk 的 hash 不变，浏览器缓存可复用，首屏更快。
      splitChunks: {
        chunks: 'all',
        cacheGroups: {
          elementUI: {
            name: 'chunk-element-ui',
            test: /[\\/]node_modules[\\/]element-ui[\\/]/,
            priority: 30
          },
          echarts: {
            name: 'chunk-echarts',
            test: /[\\/]node_modules[\\/](echarts|zrender)[\\/]/,
            priority: 25
          },
          vendors: {
            name: 'chunk-vendors',
            test: /[\\/]node_modules[\\/]/,
            priority: 10
          }
        }
      }
    },
    // Element UI 全量引入决定了打包体积本就偏大（约 1.2MB），这是项目选型的结果而非缺陷。
    // 已在上面做了 chunk 拆分，这里把 webpack 的性能提示阈值调到与实际匹配，避免刷无意义告警。
    performance: {
      hints: 'warning',
      maxEntrypointSize: 2048000,
      maxAssetSize: 1048576
    }
  },
  css: {
    loaderOptions: {
      sass: {
        // 使用 dart-sass（已替代 node-sass，兼容 Node 16/18）
        implementation: require('sass'),
        sassOptions: {
          indentedSyntax: false,
          // 静默 Dart Sass 对 legacy JS API / @import 的弃用提示。
          // ele-ui 自带样式仍用 @import，短期内无法全量迁移至 @use，
          // 这些提示属于第三方依赖的演进噪音，不影响产物正确性。
          silenceDeprecations: ['legacy-js-api', 'import']
        }
      }
    }
  }
}
