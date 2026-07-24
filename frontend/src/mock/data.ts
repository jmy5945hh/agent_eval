import type {
  AgentProduct,
  AgentVersion,
  CaseRun,
  ErrorInfo,
  EvaluationCase,
  EvaluationTask,
  ModelConfig,
  ScoringStandard,
  TrajectoryEntry,
} from '../types';

// ===== Mock 数据层：Agent / 模型 / 案例 / 评分标准 / 历史任务 =====

export const AGENTS: AgentProduct[] = [
  {
    id: 'pi-agent',
    name: 'Pi Agent',
    version: 'v2.3.1',
    vendor: 'Pi 团队',
    desc: '面向企业研发场景的智能编码 Agent，支持仓库级理解、多文件编辑与自验证。',
    status: 'enabled',
  },
  {
    id: 'devagent-cli',
    name: 'DevAgent CLI',
    version: 'v1.8.0',
    vendor: 'DevAgent 团队',
    desc: '命令行形态的 Coding Agent，轻量接入 CI/本地开发流程，擅长脚本化任务。',
    status: 'enabled',
  },
  {
    id: 'opencode',
    name: 'OpenCode',
    version: 'v0.9.4',
    vendor: '开源社区',
    desc: '开源终端编程助手，支持多模型接入与插件扩展，社区生态活跃。',
    status: 'enabled',
  },
];

export const AGENT_VERSIONS: AgentVersion[] = [
  { id: 'pi-v231', agentId: 'pi-agent', version: 'v2.3.1', releasedAt: '2026-07-08', notes: '增强仓库级检索与多文件修改稳定性', latest: true, enabled: true },
  { id: 'pi-v224', agentId: 'pi-agent', version: 'v2.2.4', releasedAt: '2026-06-12', notes: '生产稳定版，适合回归对比', enabled: true },
  { id: 'pi-v210', agentId: 'pi-agent', version: 'v2.1.0', releasedAt: '2026-05-16', notes: '旧版规划器与工具调用链路', enabled: true },
  { id: 'dev-v180', agentId: 'devagent-cli', version: 'v1.8.0', releasedAt: '2026-07-01', notes: '优化 CLI 会话恢复与日志输出', latest: true, enabled: true },
  { id: 'dev-v172', agentId: 'devagent-cli', version: 'v1.7.2', releasedAt: '2026-06-06', notes: '稳定版本，用于版本效果基线', enabled: true },
  { id: 'dev-v160', agentId: 'devagent-cli', version: 'v1.6.0', releasedAt: '2026-04-28', notes: '旧版命令执行引擎', enabled: true },
  { id: 'open-v094', agentId: 'opencode', version: 'v0.9.4', releasedAt: '2026-07-10', notes: '新增模型路由与插件隔离', latest: true, enabled: true },
  { id: 'open-v090', agentId: 'opencode', version: 'v0.9.0', releasedAt: '2026-06-18', notes: '稳定版插件系统', enabled: true },
  { id: 'open-v082', agentId: 'opencode', version: 'v0.8.2', releasedAt: '2026-05-21', notes: '旧版终端交互协议', enabled: true },
];

export const MODEL_TIERS = ['Fast', 'Pro', 'Ultra'];

export const MODELS: ModelConfig[] = [
  { id: 'm-fast', name: 'Fast-1.2' , enabled: true, scoring: false},
  { id: 'm-fast-code', name: 'Fast-Code', enabled: true, scoring: false },
  { id: 'm-pro', name: 'Pro-3.0', enabled: true, scoring: false },
  { id: 'm-pro-code', name: 'Pro-Code',enabled: true, scoring: false},
 ];

export const CATEGORIES = ['前端', 'Java后端', 'Python后端', 'AI智能体', '安全测试'];

export const DIFFICULTIES = ['高', '中', '低'];
export const IMPORTANCES = ['高', '中', '低'];

export const CASES: EvaluationCase[] = [
  {
    id: 'C001', code: 'FE-001', name: '实现响应式商品卡片列表',
    prompt: '在仓库 mall-web 的 feature/cards 分支上，实现一个响应式商品卡片列表组件 ProductGrid：\n1. 桌面端 4 列、平板 2 列、手机 1 列；\n2. 卡片包含封面图、标题、价格与"加入购物车"按钮；\n3. 价格保留两位小数并带千分位；\n4. 无封面图时使用占位图。\n请直接修改 src/components/ProductGrid.jsx 并补充必要样式。',
    repo: 'git.example.com/shop/mall-web', branch: 'feature/cards',
    category: '前端', difficulty: '中', importance: '高', version: 3,
    remark: '重点关注响应式断点与价格格式化。',
    createdAt: '2026-06-02 10:20',
    standardAnswer: [
      { path: 'src/components/ProductGrid.jsx', content: 'import React from "react";\nimport "./ProductGrid.css";\n\nconst fmtPrice = (n) =>\n  "¥" + Number(n).toFixed(2).replace(/\\B(?=(\\d{3})+(?!\\d))/g, ",");\n\nexport default function ProductGrid({ items, onAdd }) {\n  return (\n    <div className="product-grid">\n      {items.map((it) => (\n        <div className="product-card" key={it.id}>\n          <img src={it.cover || "/placeholder.png"} alt={it.title} />\n          <h3>{it.title}</h3>\n          <p className="price">{fmtPrice(it.price)}</p>\n          <button onClick={() => onAdd(it)}>加入购物车</button>\n        </div>\n      ))}\n    </div>\n  );\n}' },
      { path: 'src/components/ProductGrid.css', content: '.product-grid {\n  display: grid;\n  gap: 16px;\n  grid-template-columns: repeat(4, 1fr);\n}\n@media (max-width: 1024px) {\n  .product-grid { grid-template-columns: repeat(2, 1fr); }\n}\n@media (max-width: 600px) {\n  .product-grid { grid-template-columns: 1fr; }\n}\n.product-card .price { color: #d9764a; font-weight: 600; }' },
    ],
  },
  {
    id: 'C002', code: 'FE-002', name: '修复表格分页状态错乱',
    prompt: '仓库 admin-console 的 main 分支上，用户列表页切换页码后搜索条件被重置。请定位问题并修复：切换分页时应保留当前搜索关键词与筛选状态。相关文件：src/pages/UserList.jsx、src/hooks/useTableQuery.js。',
    repo: 'git.example.com/ops/admin-console', branch: 'main',
    category: '前端', difficulty: '中', importance: '中', version: 1,
    remark: '', createdAt: '2026-06-05 14:02',
    standardAnswer: [
      { path: 'src/hooks/useTableQuery.js', content: '// 修复：分页变化时合并而非覆盖查询状态\nexport function useTableQuery(init) {\n  const [query, setQuery] = React.useState(init);\n  const setPage = (page) =>\n    setQuery((q) => ({ ...q, page })); // 旧实现直接 setQuery({ page })\n  const setFilter = (f) =>\n    setQuery((q) => ({ ...q, ...f, page: 1 }));\n  return { query, setPage, setFilter };\n}' },
    ],
  },
  {
    id: 'C003', code: 'FE-003', name: '实现深色模式切换',
    prompt: '为 docs-site 添加深色模式：\n1. 顶栏增加切换按钮（太阳/月亮图标）；\n2. 主题偏好持久化到 localStorage；\n3. 首次访问跟随系统 prefers-color-scheme；\n4. 使用 CSS 变量实现，避免闪烁。',
    repo: 'git.example.com/team/docs-site', branch: 'main',
    category: '前端', difficulty: '低', importance: '低', version: 2,
    remark: '注意首屏闪烁问题。', createdAt: '2026-06-08 09:40',
    standardAnswer: [
      { path: 'src/theme.js', content: 'const KEY = "docs-theme";\nexport function initTheme() {\n  const saved = localStorage.getItem(KEY);\n  const sys = matchMedia("(prefers-color-scheme: dark)").matches\n    ? "dark" : "light";\n  document.documentElement.dataset.theme = saved || sys;\n}\nexport function toggleTheme() {\n  const next =\n    document.documentElement.dataset.theme === "dark" ? "light" : "dark";\n  document.documentElement.dataset.theme = next;\n  localStorage.setItem(KEY, next);\n}' },
    ],
  },
  {
    id: 'C004', code: 'BE-J-001', name: '订单接口幂等性改造',
    prompt: '仓库 trade-service（Java/Spring Boot）develop 分支，POST /api/orders 在客户端重试时会产生重复订单。请基于 Idempotency-Key 请求头实现幂等：\n1. 相同 key 的重复请求返回首个结果；\n2. key 24 小时过期；\n3. 并发相同 key 仅放行一个请求。',
    repo: 'git.example.com/trade/trade-service', branch: 'develop',
    category: 'Java后端', difficulty: '高', importance: '高', version: 4,
    remark: '考察分布式锁与缓存过期设计。', createdAt: '2026-06-10 11:15',
    standardAnswer: [
      { path: 'src/main/java/com/trade/IdempotencyFilter.java', content: '@Component\npublic class IdempotencyFilter extends OncePerRequestFilter {\n  private final StringRedisTemplate redis;\n  @Override\n  protected void doFilterInternal(HttpServletRequest req,\n      HttpServletResponse res, FilterChain chain) throws IOException {\n    String key = req.getHeader("Idempotency-Key");\n    if (key == null || !req.getMethod().equals("POST")) {\n      chain.doFilter(req, res); return;\n    }\n    Boolean ok = redis.opsForValue()\n        .setIfAbsent("idem:" + key, "LOCK", 24, TimeUnit.HOURS);\n    if (Boolean.FALSE.equals(ok)) {\n      res.setStatus(409);\n      res.getWriter().write("{\\"code\\":\\"DUPLICATE\\"}");\n      return;\n    }\n    chain.doFilter(req, res);\n  }\n}' },
    ],
  },
  {
    id: 'C005', code: 'BE-J-002', name: '补全库存扣减单元测试',
    prompt: '为 inventory-service 的 StockService.deduct 方法补全单元测试，覆盖：\n1. 正常扣减；\n2. 库存不足抛 InsufficientStockException；\n3. 并发扣减不超卖（使用 32 线程模拟）；\n4. 数量为 0 / 负数的参数校验。',
    repo: 'git.example.com/trade/inventory-service', branch: 'main',
    category: 'Java后端', difficulty: '中', importance: '中', version: 1,
    remark: '', createdAt: '2026-06-12 16:30',
    standardAnswer: [
      { path: 'src/test/java/com/trade/StockServiceTest.java', content: 'class StockServiceTest {\n  @Test void deduct_ok() { /* ... */ }\n  @Test void deduct_insufficient() {\n    assertThrows(InsufficientStockException.class,\n      () -> service.deduct("SKU1", 999));\n  }\n  @Test void deduct_concurrent() throws Exception {\n    ExecutorService pool = Executors.newFixedThreadPool(32);\n    // 32 线程各扣 1 件，初始库存 100，最终不得为负\n  }\n  @Test void deduct_invalid_qty() {\n    assertThrows(IllegalArgumentException.class,\n      () -> service.deduct("SKU1", 0));\n  }\n}' },
    ],
  },
  {
    id: 'C006', code: 'BE-P-001', name: '实现限流中间件',
    prompt: '在 py-gateway（FastAPI）main 分支实现滑动窗口限流中间件：\n1. 按 client IP 限流，默认 100 次/分钟；\n2. 超限返回 429 与 Retry-After 头；\n3. 窗口算法基于 Redis ZSET；\n4. 路径白名单可配置。',
    repo: 'git.example.com/infra/py-gateway', branch: 'main',
    category: 'Python后端', difficulty: '高', importance: '高', version: 2,
    remark: '', createdAt: '2026-06-15 10:05',
    standardAnswer: [
      { path: 'app/middleware/ratelimit.py', content: 'import time\nfrom starlette.middleware.base import BaseHTTPMiddleware\n\nclass RateLimitMiddleware(BaseHTTPMiddleware):\n    def __init__(self, app, redis, limit=100, window=60, whitelist=None):\n        super().__init__(app)\n        self.redis, self.limit, self.window = redis, limit, window\n        self.whitelist = set(whitelist or ["/healthz"])\n\n    async def dispatch(self, request, call_next):\n        if request.url.path in self.whitelist:\n            return await call_next(request)\n        key = f"rl:{request.client.host}"\n        now = time.time()\n        pipe = self.redis.pipeline()\n        pipe.zremrangebyscore(key, 0, now - self.window)\n        pipe.zcard(key)\n        _, count = await pipe.execute()\n        if count >= self.limit:\n            return JSONResponse({"detail": "Too Many Requests"},\n                status_code=429, headers={"Retry-After": "30"})\n        await self.redis.zadd(key, {str(now): now})\n        await self.redis.expire(key, self.window)\n        return await call_next(request)' },
    ],
  },
  {
    id: 'C007', code: 'BE-P-002', name: '数据导出任务异步化',
    prompt: 'report-center 的导出接口在数据量大时同步阻塞导致超时。请改造为异步任务：\n1. 提交导出返回 task_id；\n2. Celery 后台生成 CSV 上传 OSS；\n3. GET /api/export/<task_id> 查询状态与下载链接；\n4. 失败可重试 3 次。',
    repo: 'git.example.com/bi/report-center', branch: 'feature/async-export',
    category: 'Python后端', difficulty: '中', importance: '中', version: 1,
    remark: '', createdAt: '2026-06-18 15:44',
    standardAnswer: [
      { path: 'tasks/export_tasks.py', content: '@celery.task(bind=True, max_retries=3)\ndef export_csv(self, task_id, query):\n    try:\n        rows = db.query(query)\n        path = write_csv(rows)\n        url = oss.upload(path)\n        store.update(task_id, status="DONE", url=url)\n    except Exception as exc:\n        raise self.retry(exc=exc, countdown=2 ** self.request.retries)' },
    ],
  },
  {
    id: 'C008', code: 'AG-001', name: '为 Agent 增加工具调用重试',
    prompt: '在 agent-core 的 main 分支上，工具调用失败（网络抖动/超时）时直接终止任务。请增加指数退避重试：\n1. 最多重试 3 次，初始间隔 1s，倍率 2；\n2. 仅对可重试错误（超时、5xx、连接重置）重试；\n3. 重试过程写入轨迹日志。',
    repo: 'git.example.com/agent/agent-core', branch: 'main',
    category: 'AI智能体', difficulty: '高', importance: '高', version: 2,
    remark: '', createdAt: '2026-06-20 13:21',
    standardAnswer: [
      { path: 'core/tool_runner.py', content: 'async def call_with_retry(fn, *, retries=3, base=1.0, tracer=None):\n    for attempt in range(retries):\n        try:\n            return await fn()\n        except RetryableError as e:\n            if attempt == retries - 1:\n                raise\n            wait = base * (2 ** attempt)\n            tracer and tracer.log(\n                f"tool retry {attempt + 1}/{retries} after {wait}s: {e}")\n            await asyncio.sleep(wait)' },
    ],
  },
  {
    id: 'C009', code: 'AG-002', name: '实现对话上下文压缩',
    prompt: 'agent-core 在长任务中上下文超出窗口。请实现上下文压缩策略：\n1. 当 token 数超过阈值 80% 时触发；\n2. 保留系统提示与最近 5 轮对话；\n3. 早期对话由摘要模型压缩为要点列表；\n4. 提供压缩前后 token 统计。',
    repo: 'git.example.com/agent/agent-core', branch: 'main',
    category: 'AI智能体', difficulty: '高', importance: '中', version: 1,
    remark: '', createdAt: '2026-06-22 09:18',
    standardAnswer: [
      { path: 'core/context_compactor.py', content: 'class ContextCompactor:\n    def __init__(self, summarizer, keep_recent=5, threshold=0.8):\n        self.summarizer, self.keep_recent = summarizer, keep_recent\n        self.threshold = threshold\n\n    async def compact(self, messages, max_tokens):\n        used = count_tokens(messages)\n        if used < max_tokens * self.threshold:\n            return messages, {"before": used, "after": used}\n        head = [m for m in messages if m.role == "system"]\n        tail = messages[-self.keep_recent:]\n        middle = messages[len(head):-self.keep_recent]\n        summary = await self.summarizer.summarize(middle)\n        out = head + [Message(role="system", content=summary)] + tail\n        return out, {"before": used, "after": count_tokens(out)}' },
    ],
  },
  {
    id: 'C010', code: 'SEC-001', name: '修复 SQL 注入风险点',
    prompt: '审计发现 user-center 的查询接口存在 SQL 注入风险：GET /api/users?sort=<column> 直接拼接排序字段。请修复：\n1. 排序字段使用白名单校验；\n2. 所有动态 SQL 改为参数绑定；\n3. 补充注入用例的自动化测试。',
    repo: 'git.example.com/iam/user-center', branch: 'hotfix/sqli',
    category: '安全测试', difficulty: '中', importance: '高', version: 3,
    remark: '安全红线案例，必须通过注入用例。', createdAt: '2026-06-24 17:02',
    standardAnswer: [
      { path: 'dao/user_dao.py', content: 'ALLOWED_SORT = {"id", "name", "created_at"}\n\ndef list_users(sort: str = "id", desc: bool = False):\n    if sort not in ALLOWED_SORT:\n        raise ValueError(f"illegal sort field: {sort}")\n    direction = "DESC" if desc else "ASC"\n    # 字段名经白名单校验后可安全拼接；值一律参数绑定\n    sql = f"SELECT * FROM users ORDER BY {sort} {direction}"\n    return db.execute(sql)' },
    ],
  },
  {
    id: 'C011', code: 'SEC-002', name: '接口越权访问治理',
    prompt: 'file-server 的 GET /api/files/<id> 未校验归属，用户可遍历 id 下载他人文件（IDOR）。请修复：\n1. 校验文件归属或显式授权；\n2. 无权限返回 403 且不回显文件是否存在；\n3. 增加越权访问审计日志。',
    repo: 'git.example.com/doc/file-server', branch: 'main',
    category: '安全测试', difficulty: '中', importance: '高', version: 1,
    remark: '', createdAt: '2026-06-26 10:37',
    standardAnswer: [
      { path: 'app/files/views.py', content: '@router.get("/api/files/{file_id}")\nasync def get_file(file_id: str, user=Depends(current_user)):\n    f = await repo.get(file_id)\n    if not f or (f.owner_id != user.id and not f.shared_with(user.id)):\n        audit.log("IDOR_ATTEMPT", user=user.id, file=file_id)\n        raise HTTPException(403, "forbidden")\n    return FileResponse(f.path)' },
    ],
  },
  {
    id: 'C012', code: 'FE-004', name: '大列表虚拟滚动优化',
    prompt: 'admin-console 的操作日志页一次渲染 5000+ 行导致卡顿。请引入虚拟滚动：\n1. 仅渲染可视区行（行高固定 48px）；\n2. 滚动 FPS 不低于 50；\n3. 保留行选中与展开能力；\n4. 不新增重型依赖（可自行实现）。',
    repo: 'git.example.com/ops/admin-console', branch: 'perf/log-list',
    category: '前端', difficulty: '高', importance: '中', version: 1,
    remark: '', createdAt: '2026-06-28 14:55',
    standardAnswer: [
      { path: 'src/components/VirtualList.jsx', content: 'export default function VirtualList({ rows, rowHeight = 48, height = 600, renderRow }) {\n  const [scrollTop, setScrollTop] = React.useState(0);\n  const start = Math.floor(scrollTop / rowHeight);\n  const count = Math.ceil(height / rowHeight) + 2;\n  const visible = rows.slice(start, start + count);\n  return (\n    <div style={{ height, overflow: "auto" }}\n      onScroll={(e) => setScrollTop(e.currentTarget.scrollTop)}>\n      <div style={{ height: rows.length * rowHeight, position: "relative" }}>\n        {visible.map((row, i) => (\n          <div key={row.id} style={{\n            position: "absolute", top: (start + i) * rowHeight,\n            height: rowHeight, width: "100%"\n          }}>{renderRow(row)}</div>\n        ))}\n      </div>\n    </div>\n  );\n}' },
    ],
  },
];

// ===== 评分标准（带版本管理）=====
export const SCORING_STANDARDS: ScoringStandard[] = [
  {
    id: 'std-v1',
    version: 'v1.0',
    updatedAt: '2026-05-20 10:00',
    note: '首版通用评分标准。',
    dimensions: [
      { key: 'correctness', label: '正确性', weight: 40, desc: '功能结果是否正确、边界是否覆盖' },
      { key: 'completeness', label: '完整性', weight: 20, desc: '需求点是否全部实现' },
      { key: 'quality', label: '代码质量', weight: 25, desc: '可读性、结构、可维护性' },
      { key: 'standard', label: '规范性', weight: 15, desc: '是否符合仓库既有规范' },
    ],
  },
  {
    id: 'std-v2',
    version: 'v2.0',
    current: true,
    updatedAt: '2026-06-30 15:30',
    note: '增加安全性维度，调整权重以适配安全测试类案例。',
    dimensions: [
      { key: 'correctness', label: '正确性', weight: 35, desc: '功能结果是否正确、边界是否覆盖' },
      { key: 'completeness', label: '功能完整性', weight: 25, desc: '需求点是否全部实现' },
      { key: 'quality', label: '代码质量', weight: 20, desc: '可读性、结构、可维护性' },
      { key: 'security', label: '安全性', weight: 10, desc: '是否引入安全风险' },
      { key: 'standard', label: '规范性', weight: 10, desc: '是否符合仓库既有规范' },
    ],
  },
];

export const currentStandard = (standards: ScoringStandard[]) =>
  standards.find((s) => s.current) || standards[standards.length - 1];

// ===== 执行轨迹 / 错误日志生成器（供引擎与种子任务复用）=====
const pick = <T,>(arr: T[]) => arr[Math.floor(Math.random() * arr.length)];

export function genTrajectory(caseItem: EvaluationCase, ok: boolean): TrajectoryEntry[] {
  const now = Date.now();
  const t = (offset: number) => new Date(now + offset).toTimeString().slice(0, 8);
  const answer = caseItem.standardAnswer[0]!;
  const msgs: TrajectoryEntry[] = [
    { role: 'user', kind: 'text', time: t(0), content: caseItem.prompt },
    { role: 'agent', kind: 'think', time: t(4_000), content: `收到任务。我先梳理仓库结构，定位与「${caseItem.name}」相关的模块，再制定修改计划。` },
    { role: 'tool', kind: 'cmd', time: t(6_000), title: '执行命令', content: `$ git clone ${caseItem.repo} -b ${caseItem.branch}\n$ tree -L 2\n.\n├── src/\n├── package.json\n└── README.md` },
    { role: 'agent', kind: 'think', time: t(9_000), content: `已定位目标文件，共涉及 ${caseItem.standardAnswer.length} 个文件。开始实施修改。` },
    { role: 'tool', kind: 'edit', time: t(12_000), title: `编辑 ${answer.path}`, content: answer.content },
    { role: 'tool', kind: 'cmd', time: t(16_000), title: '运行验证', content: ok ? '$ npm test\n✓ 12 passed, 0 failed (3.2s)' : '$ npm test\n✗ 2 failed, 10 passed (4.1s)' },
  ];
  if (ok) {
    msgs.push({ role: 'agent', kind: 'text', time: t(18_000), content: `已完成「${caseItem.name}」：所有需求点均已实现，测试通过。修改已提交至 ${caseItem.branch} 分支。` });
  } else {
    msgs.push({ role: 'agent', kind: 'text', time: t(18_000), content: `执行中断：验证阶段出现未通过用例，已停止提交，详见错误日志。` });
  }
  return msgs;
}

export const ERROR_POOL = [
  {
    category: '调度失败',
    log: '[scheduler] sandbox provisioning timeout after 120s\n[scheduler] node pool exhausted: no available runner (cpu=8, mem=16Gi)\n[scheduler] ERROR: environment bootstrap failed',
  },
  {
    category: 'Agent内部错误',
    log: '[agent] tool loop detected: same call repeated 6 times (read_file)\n[agent] context window overflow: 128k tokens exceeded\n[agent] FATAL: planner aborted after 3 consecutive parse errors',
  },
  {
    category: '其他错误',
    log: '[net] ECONNRESET: connection to model gateway reset by peer\n[net] retry 3/3 failed\n[task] aborted due to unrecoverable network error',
  },
];

export function genError(): ErrorInfo {
  return pick(ERROR_POOL);
}

// ===== 历史测评任务（已完成，供记录追溯演示）=====
function seedRuns(caseIds: string[], failIdx: number[], standard: ScoringStandard): CaseRun[] {
  return caseIds.map((cid, i) => {
    const c = CASES.find((x) => x.id === cid)!;
    const ok = !failIdx.includes(i);
    const err = ok ? null : genError();
    const dur = 60_000 + Math.floor(Math.random() * 240_000);
    const run: CaseRun = {
      caseId: cid,
      status: ok ? 'success' : 'failed',
      attempts: 1,
      removed: false,
      removeReason: '',
      rounds: 4 + Math.floor(Math.random() * 8),
      tokensIn: 12000 + Math.floor(Math.random() * 30000),
      tokensOut: 2500 + Math.floor(Math.random() * 9000),
      durationMs: dur,
      error: err,
      trajectory: genTrajectory(c, ok),
      score: null,
    };
    const base = ok ? 62 + Math.random() * 33 : 8 + Math.random() * 30;
    const dims: Record<string, number> = {};
    const comments: Record<string, string> = {};
    standard.dimensions.forEach((d) => {
      const v = Math.max(0, Math.min(100, Math.round(base + (Math.random() * 16 - 8))));
      dims[d.key] = v;
      comments[d.key] = ok
        ? `${d.label}表现${v >= 85 ? '优秀' : v >= 70 ? '良好' : '一般'}，${d.desc}方面${v >= 70 ? '基本达标' : '仍有提升空间'}。`
        : `任务未成功完成，${d.label}得分较低；建议修复执行环境后重跑。`;
    });
    run.score = {
      dims,
      comments,
      analysis: ok
        ? `整体实现覆盖了「${c.name}」的主要需求点，方案合理。建议补充更多边界用例验证。`
        : `案例「${c.name}」执行失败（${err?.category || '未知错误'}），无法形成有效产出，评分按失败处理。`,
      note: '',
      edited: false,
      model: 'Judge-Pro',
      standardVersion: standard.version,
    };
    return run;
  });
}

export function seedTasks(): EvaluationTask[] {
  const std = SCORING_STANDARDS[1];
  return [
    {
      id: 'T-20260715-01',
      name: 'Pi Agent_Ultra-3.5_202607151030_赵启铭',
      agentId: 'pi-agent', modelId: 'm-ultra',
      creator: '赵启铭', createdAt: '2026-07-15 10:30',
      status: 'completed', phase: 'done',
      scoringModelId: 'judge-pro', standardVersion: 'v2.0',
      scoringStatus: 'confirmed',
      runs: seedRuns(['C001', 'C004', 'C006', 'C008', 'C010'], [3], std),
    },
    {
      id: 'T-20260718-02',
      name: 'OpenCode_Pro-3.0_202607181415_赵启铭',
      agentId: 'opencode', modelId: 'm-pro',
      creator: '赵启铭', createdAt: '2026-07-18 14:15',
      status: 'completed', phase: 'done',
      scoringModelId: 'judge-pro', standardVersion: 'v2.0',
      scoringStatus: 'confirmed',
      runs: seedRuns(['C002', 'C003', 'C005', 'C012'], [1], std),
    },
    {
      id: 'T-20260720-03',
      name: 'DevAgent CLI_Fast-1.2_202607200930_林晚晴',
      agentId: 'devagent-cli', modelId: 'm-fast',
      creator: '林晚晴', createdAt: '2026-07-20 09:30',
      status: 'cancelled', phase: 'done',
      scoringModelId: null, standardVersion: null,
      scoringStatus: 'idle',
      runs: seedRuns(['C001', 'C007'], [], std).map((r) => ({ ...r, score: null, status: r.caseId === 'C001' ? 'success' as const : 'cancelled' as const })),
    },
  ];
}

export const USERS = ['赵启铭', '林晚晴', '陈叙白'];
