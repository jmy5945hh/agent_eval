# Agent Eval

Agent 智能测评系统前端，用于配置测评任务、管理案例、跟踪执行过程并查看评分结果。当前版本使用本地 Mock 数据，适合作为产品演示与前端交互原型。

## 本地使用

环境要求：Node.js 20+、npm 10+。

```bash
# 安装依赖
npm install

# 启动开发环境
npm run dev
```

启动后访问终端输出的地址，默认通常为 `http://localhost:5173`。

```bash
npm run build      # 类型检查并构建生产包
npm run preview    # 本地预览生产包
npm run typecheck  # TypeScript 类型检查
npm run lint       # 代码检查
```

## 核心特性

- 测评工作台：关键指标、Agent 排行与近期任务概览
- 创建测评：分步选择 Agent、版本、模型、案例及评分标准
- 案例管理：按分类、难度等条件筛选并查看案例详情
- 执行中心：展示串行队列、执行进度、轨迹、日志与 Token 用量
- 评分管理：查看多维评分、模型分析，并支持人工确认
- 记录与配置：检索历史任务，维护 Agent、模型及版本信息
- 响应式布局：支持桌面端侧栏收起及移动端导航

## 设计原则

整体采用 **Organic × Material × Flat** 的明亮风格：

- 暖白背景与低饱和自然色，主色为森林绿，橙色用于强调
- 卡片化信息分组，以轻边框、柔和阴影和适度圆角建立层级
- 状态、进度和评分优先可视化，减少阅读与操作成本
- 统一 Ant Design 主题变量，并兼顾桌面端与移动端体验

## 技术栈

- React 19 + TypeScript
- Vite 8
- Ant Design 6 + Ant Design Icons
- Oxlint

## 项目结构

```text
frontend/
├── public/                 # 静态资源
├── src/
│   ├── components/
│   │   ├── cases/         # 案例筛选、详情与编辑
│   │   ├── common/        # 通用指标卡、选择卡
│   │   └── tasks/         # 任务表格、执行详情与评分
│   ├── mock/              # 本地演示数据
│   ├── pages/             # 工作台、创建、案例、执行、记录、设置
│   ├── types/             # 领域模型与 TypeScript 类型
│   ├── App.tsx            # 应用布局、导航与页面状态
│   ├── App.css            # 页面及响应式样式
│   └── main.tsx           # 应用入口
├── package.json
└── vite.config.js
```

## 数据与扩展

当前页面状态与数据来自 `src/mock/data.ts`，刷新后不会持久化。接入真实服务时，可将 Mock 数据替换为 API 请求，并补充路由、鉴权和全局状态管理。
