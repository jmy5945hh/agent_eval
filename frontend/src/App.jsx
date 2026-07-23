import { useMemo, useState } from 'react';
import {
  Avatar,
  Badge,
  Button,
  ConfigProvider,
  Drawer,
  Dropdown,
  Empty,
  Form,
  Input,
  InputNumber,
  Layout,
  Menu,
  Modal,
  Progress,
  Radio,
  Select,
  Space,
  Steps,
  Table,
  Tabs,
  Tag,
  Tooltip,
  Typography,
  message,
} from 'antd';
import {
  AppstoreOutlined,
  ArrowLeftOutlined,
  ArrowRightOutlined,
  BarChartOutlined,
  BellOutlined,
  BookOutlined,
  BranchesOutlined,
  CheckCircleFilled,
  ClockCircleOutlined,
  CloudServerOutlined,
  CodeOutlined,
  DatabaseOutlined,
  DeleteOutlined,
  DownOutlined,
  EditOutlined,
  ExperimentOutlined,
  ExportOutlined,
  FileSearchOutlined,
  FilterOutlined,
  HistoryOutlined,
  HomeOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  MoreOutlined,
  PlusOutlined,
  ReloadOutlined,
  RobotOutlined,
  RocketOutlined,
  SafetyCertificateOutlined,
  SearchOutlined,
  SettingOutlined,
  ThunderboltOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { RunStatusTag, ScoreStatusTag, TaskStatusTag } from './components/StatusTag';
import {
  AGENTS,
  CASES,
  CATEGORIES,
  MODELS,
  SCORING_STANDARDS,
  seedTasks,
} from './mock/data';
import './App.css';

const { Header, Sider, Content } = Layout;
const { Title, Paragraph } = Typography;

const pageMeta = {
  dashboard: ['工作台', '掌握测评进展，快速开始新一轮智能化评估'],
  create: ['创建测评', '选择 Agent、模型和案例，组装一次标准化测评'],
  cases: ['案例库', '管理可复用的真实研发场景与标准答案'],
  execution: ['执行中心', '监控任务队列、执行轨迹与自动评分'],
  records: ['测评记录', '查询、追溯并复用每一次历史测评'],
  settings: ['Agent 与模型', '管理参测 Agent 和已接入的模型配置'],
};

const navItems = [
  { key: 'dashboard', icon: <HomeOutlined />, label: '工作台' },
  { key: 'create', icon: <RocketOutlined />, label: '创建测评' },
  { key: 'cases', icon: <BookOutlined />, label: '案例库' },
  { key: 'execution', icon: <ThunderboltOutlined />, label: '执行中心' },
  { key: 'records', icon: <HistoryOutlined />, label: '测评记录' },
  { type: 'divider' },
  { key: 'settings', icon: <SettingOutlined />, label: 'Agent 与模型' },
];

const iconForCategory = {
  前端: <CodeOutlined />,
  Java后端: <CloudServerOutlined />,
  Python后端: <DatabaseOutlined />,
  AI智能体: <RobotOutlined />,
  安全测试: <SafetyCertificateOutlined />,
};

const difficultyClass = { 高: 'danger', 中: 'warning', 低: 'success' };

function MetricCard({ label, value, note, tone = 'green', icon }) {
  return (
    <div className={`metric-card tone-${tone}`}>
      <div className="metric-top">
        <span>{label}</span>
        <span className="metric-icon">{icon}</span>
      </div>
      <div className="metric-value">{value}</div>
      <div className="metric-note">{note}</div>
    </div>
  );
}

function MiniDonut({ value, label, color = '#4f7a5b' }) {
  return (
    <div className="mini-donut-wrap">
      <div
        className="mini-donut"
        style={{ background: `conic-gradient(${color} ${value}%, #ebe7df ${value}% 100%)` }}
      >
        <div><strong>{value}</strong><span>%</span></div>
      </div>
      <span>{label}</span>
    </div>
  );
}

function Dashboard({ tasks, onNavigate, onOpenTask }) {
  const recent = tasks[0];
  const totalRuns = tasks.reduce((sum, task) => sum + task.runs.length, 0);
  const successRuns = tasks.reduce(
    (sum, task) => sum + task.runs.filter((run) => run.status === 'success').length,
    0,
  );
  const latestScore = Math.round(
    recent.runs.reduce((sum, run) => {
      if (!run.score) return sum;
      const dims = Object.values(run.score.dims);
      return sum + dims.reduce((a, b) => a + b, 0) / dims.length;
    }, 0) / recent.runs.filter((run) => run.score).length,
  );

  return (
    <div className="page-stack">
      <section className="welcome-panel">
        <div className="welcome-copy">
          <div className="eyebrow"><span /> AI CODING AGENT EVALUATION</div>
          <Title level={2}>早上好，赵启铭</Title>
          <Paragraph>
            今天可以从一次标准化测评开始，验证 Agent 在真实研发场景中的表现。
          </Paragraph>
          <Space size={12} wrap>
            <Button type="primary" size="large" icon={<RocketOutlined />} onClick={() => onNavigate('create')}>
              创建测评任务
            </Button>
            <Button size="large" icon={<BookOutlined />} onClick={() => onNavigate('cases')}>
              浏览案例库
            </Button>
          </Space>
        </div>
        <div className="welcome-visual" aria-hidden="true">
          <div className="orbit orbit-one" />
          <div className="orbit orbit-two" />
          <div className="core-mark"><RobotOutlined /></div>
          <div className="floating-node node-a">Agent</div>
          <div className="floating-node node-b">Cases</div>
          <div className="floating-node node-c">Score</div>
        </div>
      </section>

      <section className="metrics-grid">
        <MetricCard label="累计测评" value={tasks.length + 21} note="本月新增 8 次" tone="green" icon={<ExperimentOutlined />} />
        <MetricCard label="已沉淀案例" value={CASES.length} note="覆盖 5 个研发分类" tone="blue" icon={<BookOutlined />} />
        <MetricCard label="整体成功率" value={`${Math.round((successRuns / totalRuns) * 100)}%`} note="较上周提升 4.2%" tone="orange" icon={<CheckCircleFilled />} />
        <MetricCard label="已接入组合" value={AGENTS.length * 3} note="3 Agents · 3 Models" tone="purple" icon={<BranchesOutlined />} />
      </section>

      <section className="dashboard-grid">
        <div className="surface-card span-8">
          <div className="section-head">
            <div>
              <h3>最近一次测评</h3>
              <p>{recent.name}</p>
            </div>
            <Button type="text" onClick={() => onOpenTask(recent)}>查看详情 <ArrowRightOutlined /></Button>
          </div>
          <div className="task-overview">
            <div className="task-score">
              <span>综合评分</span>
              <strong>{latestScore}</strong>
              <em>/ 100</em>
            </div>
            <div className="task-donuts">
              <MiniDonut value={80} label="执行成功率" />
              <MiniDonut value={92} label="案例有效率" color="#d9764a" />
            </div>
            <div className="task-summary">
              <div><span>测评对象</span><b>Pi Agent · Ultra-3.5</b></div>
              <div><span>案例范围</span><b>{recent.runs.length} 条 · 4 个分类</b></div>
              <div><span>完成时间</span><b>07-15 11:48</b></div>
            </div>
          </div>
          <div className="dimension-bars">
            {[
              ['正确性', 88],
              ['功能完整性', 84],
              ['代码质量', 91],
              ['安全性', 86],
            ].map(([label, value]) => (
              <div className="dimension-bar" key={label}>
                <span>{label}</span>
                <Progress percent={value} showInfo={false} strokeColor="#4f7a5b" trailColor="#ebe8e0" />
                <b>{value}</b>
              </div>
            ))}
          </div>
        </div>

        <div className="surface-card span-4">
          <div className="section-head">
            <div><h3>Agent 表现</h3><p>最近 30 天平均得分</p></div>
            <BarChartOutlined className="section-icon" />
          </div>
          <div className="rank-list">
            {[
              ['Pi Agent', 'v2.3.1', 88, '#4f7a5b'],
              ['OpenCode', 'v0.9.4', 82, '#d9764a'],
              ['DevAgent CLI', 'v1.8.0', 76, '#71829c'],
            ].map(([name, version, score, color], index) => (
              <div className="rank-row" key={name}>
                <span className={`rank-index rank-${index + 1}`}>{index + 1}</span>
                <div className="rank-main">
                  <div><b>{name}</b><small>{version}</small></div>
                  <Progress percent={score} showInfo={false} strokeColor={color} size="small" />
                </div>
                <strong>{score}</strong>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="surface-card">
        <div className="section-head">
          <div><h3>近期测评记录</h3><p>快速回到最近处理过的任务</p></div>
          <Button type="text" onClick={() => onNavigate('records')}>查看全部 <ArrowRightOutlined /></Button>
        </div>
        <TaskTable tasks={tasks.slice(0, 3)} onOpenTask={onOpenTask} compact />
      </section>
    </div>
  );
}

function CaseFilterBar({ search, setSearch, category, setCategory, count, extra }) {
  return (
    <div className="filter-bar">
      <Input
        prefix={<SearchOutlined />}
        placeholder="搜索案例名称、描述或仓库"
        value={search}
        onChange={(event) => setSearch(event.target.value)}
        allowClear
        className="search-input"
      />
      <Select
        value={category}
        onChange={setCategory}
        options={[{ value: 'all', label: '全部分类' }, ...CATEGORIES.map((item) => ({ value: item, label: item }))]}
        className="filter-select"
      />
      <Button icon={<FilterOutlined />}>更多筛选</Button>
      <span className="filter-count">共 {count} 条案例</span>
      {extra}
    </div>
  );
}

function CasesPage({ onSelectCase }) {
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('all');
  const [addOpen, setAddOpen] = useState(false);
  const filtered = CASES.filter((item) => {
    const matchesCategory = category === 'all' || item.category === category;
    const text = `${item.name}${item.prompt}${item.repo}`.toLowerCase();
    return matchesCategory && text.includes(search.toLowerCase());
  });

  const columns = [
    {
      title: '案例',
      dataIndex: 'name',
      width: 290,
      render: (_, record) => (
        <button className="case-link" onClick={() => onSelectCase(record)}>
          <span className="case-type-icon">{iconForCategory[record.category]}</span>
          <span><b>{record.name}</b><small>{record.code} · v{record.version}</small></span>
        </button>
      ),
    },
    {
      title: '分类',
      dataIndex: 'category',
      width: 120,
      render: (value) => <Tag className="soft-tag">{value}</Tag>,
    },
    {
      title: '难度',
      dataIndex: 'difficulty',
      width: 100,
      render: (value) => <span className={`level-pill ${difficultyClass[value]}`}><i />{value}</span>,
    },
    {
      title: '目标仓库 / 分支',
      dataIndex: 'repo',
      ellipsis: true,
      render: (_, record) => (
        <div className="repo-cell"><span>{record.repo.replace('git.example.com/', '')}</span><small><BranchesOutlined /> {record.branch}</small></div>
      ),
    },
    { title: '重要性', dataIndex: 'importance', width: 100, render: (value) => <span>{value}</span> },
    { title: '更新时间', dataIndex: 'createdAt', width: 150, render: (value) => value.slice(0, 10) },
    {
      title: '',
      width: 56,
      render: () => (
        <Dropdown menu={{ items: [{ key: 'edit', icon: <EditOutlined />, label: '编辑' }, { key: 'delete', icon: <DeleteOutlined />, label: '删除', danger: true }] }}>
          <Button type="text" icon={<MoreOutlined />} />
        </Dropdown>
      ),
    },
  ];

  return (
    <div className="page-stack">
      <div className="category-strip">
        <button className={category === 'all' ? 'active' : ''} onClick={() => setCategory('all')}>
          <span className="category-icon"><AppstoreOutlined /></span><b>全部案例</b><small>{CASES.length} 条</small>
        </button>
        {CATEGORIES.map((item) => (
          <button className={category === item ? 'active' : ''} onClick={() => setCategory(item)} key={item}>
            <span className="category-icon">{iconForCategory[item]}</span><b>{item}</b><small>{CASES.filter((c) => c.category === item).length} 条</small>
          </button>
        ))}
      </div>
      <section className="surface-card table-card">
        <div className="section-head section-head-actions">
          <div><h3>案例列表</h3><p>场景化用例、Prompt 与标准答案统一管理</p></div>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setAddOpen(true)}>新增案例</Button>
        </div>
        <CaseFilterBar search={search} setSearch={setSearch} category={category} setCategory={setCategory} count={filtered.length} />
        <Table
          dataSource={filtered}
          columns={columns}
          rowKey="id"
          pagination={{ pageSize: 8, showSizeChanger: false }}
          scroll={{ x: 960 }}
        />
      </section>
      <CaseFormModal open={addOpen} onCancel={() => setAddOpen(false)} />
    </div>
  );
}

function CaseFormModal({ open, onCancel }) {
  const [form] = Form.useForm();
  return (
    <Modal
      open={open}
      title="新增测评案例"
      width={720}
      onCancel={onCancel}
      okText="保存案例"
      cancelText="取消"
      onOk={() => {
        form.validateFields().then(() => {
          message.success('案例已保存（Mock）');
          form.resetFields();
          onCancel();
        });
      }}
    >
      <Form form={form} layout="vertical" className="modal-form" initialValues={{ difficulty: '中', category: '前端', version: 1 }}>
        <div className="form-grid">
          <Form.Item name="name" label="案例名称" rules={[{ required: true, message: '请输入案例名称' }]}>
            <Input maxLength={20} placeholder="例如：实现响应式商品卡片" />
          </Form.Item>
          <Form.Item name="category" label="案例分类" rules={[{ required: true }]}>
            <Select options={CATEGORIES.map((value) => ({ value, label: value }))} />
          </Form.Item>
        </div>
        <Form.Item name="prompt" label="Prompt 描述" rules={[{ required: true, message: '请输入完整 Prompt' }]}>
          <Input.TextArea rows={5} placeholder="输入将提供给 Agent 的完整任务描述与验收要求" />
        </Form.Item>
        <div className="form-grid">
          <Form.Item name="repo" label="目标仓库" rules={[{ required: true }]}>
            <Input placeholder="git.example.com/team/project" />
          </Form.Item>
          <Form.Item name="branch" label="目标分支" rules={[{ required: true }]}>
            <Input placeholder="main" />
          </Form.Item>
          <Form.Item name="difficulty" label="难度">
            <Select options={['高', '中', '低'].map((value) => ({ value, label: value }))} />
          </Form.Item>
          <Form.Item name="version" label="版本">
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
        </div>
        <Form.Item name="answer" label="标准答案" rules={[{ required: true, message: '请填写标准答案或外联地址' }]}>
          <Input.TextArea rows={3} placeholder="代码内容、相对路径或标准答案外联地址" />
        </Form.Item>
      </Form>
    </Modal>
  );
}

function CaseDrawer({ item, onClose }) {
  return (
    <Drawer open={Boolean(item)} onClose={onClose} width={640} title={item?.name} extra={<Button icon={<EditOutlined />}>编辑</Button>}>
      {item && (
        <div className="drawer-stack">
          <div className="case-detail-hero">
            <div className="case-type-icon large">{iconForCategory[item.category]}</div>
            <div><Tag className="soft-tag">{item.category}</Tag><span className={`level-pill ${difficultyClass[item.difficulty]}`}><i />{item.difficulty}难度</span></div>
            <p>{item.code} · 版本 v{item.version} · 创建于 {item.createdAt}</p>
          </div>
          <section>
            <h4>Prompt 描述</h4>
            <div className="prompt-box">{item.prompt}</div>
          </section>
          <section>
            <h4>执行目标</h4>
            <div className="detail-list">
              <div><span>仓库</span><b>{item.repo}</b></div>
              <div><span>分支</span><b>{item.branch}</b></div>
              <div><span>重要性</span><b>{item.importance}</b></div>
            </div>
          </section>
          <section>
            <h4>标准答案 <Tag>{item.standardAnswer.length} 个文件</Tag></h4>
            {item.standardAnswer.map((file) => (
              <div className="code-file" key={file.path}>
                <div className="code-file-head"><span>{file.path}</span><span>标准实现</span></div>
                <pre className="code-block">{file.content}</pre>
              </div>
            ))}
          </section>
        </div>
      )}
    </Drawer>
  );
}

function PickCard({ active, onClick, title, subtitle, description, meta, icon }) {
  return (
    <button className={`selection-card ${active ? 'active' : ''}`} onClick={onClick}>
      <span className="selection-check">{active ? <CheckCircleFilled /> : null}</span>
      <span className="selection-icon">{icon}</span>
      <span className="selection-title">{title}<small>{subtitle}</small></span>
      <span className="selection-desc">{description}</span>
      <span className="selection-meta">{meta}</span>
    </button>
  );
}

function CreateTask({ onLaunch }) {
  const [step, setStep] = useState(0);
  const [agentId, setAgentId] = useState(AGENTS[0].id);
  const availableModels = MODELS.filter((model) => model.enabled && !model.scoring);
  const [modelId, setModelId] = useState(availableModels[1].id);
  const [selectedCases, setSelectedCases] = useState(['C001', 'C004', 'C008', 'C010']);
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('all');
  const [taskName, setTaskName] = useState('');
  const filtered = CASES.filter(
    (item) =>
      (category === 'all' || item.category === category) &&
      `${item.name}${item.prompt}${item.repo}`.toLowerCase().includes(search.toLowerCase()),
  );

  const stepContent = [
    <div className="wizard-section" key="agent">
      <div className="wizard-intro"><span>01</span><div><h3>选择参测 Agent</h3><p>一期支持 3 类 Coding Agent，本次测评仅可选择一个。</p></div></div>
      <div className="selection-grid">
        {AGENTS.map((agent) => (
          <PickCard
            key={agent.id}
            active={agentId === agent.id}
            onClick={() => setAgentId(agent.id)}
            title={agent.name}
            subtitle={agent.version}
            description={agent.desc}
            meta={`${agent.vendor} · 已启用`}
            icon={<RobotOutlined />}
          />
        ))}
      </div>
    </div>,
    <div className="wizard-section" key="model">
      <div className="wizard-intro"><span>02</span><div><h3>选择测评模型</h3><p>仅展示已启用且配置完成的模型。</p></div></div>
      <div className="selection-grid">
        {availableModels.map((model) => (
          <PickCard
            key={model.id}
            active={modelId === model.id}
            onClick={() => setModelId(model.id)}
            title={model.name}
            subtitle={model.tier}
            description={model.desc}
            meta={`${model.provider} · ${model.version}`}
            icon={<DatabaseOutlined />}
          />
        ))}
      </div>
    </div>,
    <div className="wizard-section" key="cases">
      <div className="wizard-intro"><span>03</span><div><h3>选择测评案例</h3><p>可跨分类组合案例，已选择 {selectedCases.length} 条。</p></div></div>
      <CaseFilterBar
        search={search}
        setSearch={setSearch}
        category={category}
        setCategory={setCategory}
        count={filtered.length}
        extra={<div className="selected-count"><CheckCircleFilled /> 已选 {selectedCases.length}</div>}
      />
      <Table
        rowSelection={{
          selectedRowKeys: selectedCases,
          onChange: setSelectedCases,
        }}
        dataSource={filtered}
        rowKey="id"
        pagination={{ pageSize: 6, showSizeChanger: false }}
        columns={[
          { title: '案例', dataIndex: 'name', render: (_, record) => <div className="simple-case"><b>{record.name}</b><small>{record.code}</small></div> },
          { title: '分类', dataIndex: 'category', render: (value) => <Tag className="soft-tag">{value}</Tag> },
          { title: '难度', dataIndex: 'difficulty', width: 90, render: (value) => <span className={`level-pill ${difficultyClass[value]}`}><i />{value}</span> },
          { title: '仓库', dataIndex: 'repo', ellipsis: true, render: (value) => value.replace('git.example.com/', '') },
        ]}
      />
    </div>,
    <div className="wizard-section" key="confirm">
      <div className="wizard-intro"><span>04</span><div><h3>确认测评配置</h3><p>核对无误后，任务将进入串行执行队列。</p></div></div>
      <div className="confirm-grid">
        <div className="confirm-main">
          <label>任务名称 <small>选填</small></label>
          <Input
            size="large"
            value={taskName}
            onChange={(event) => setTaskName(event.target.value)}
            placeholder={`${AGENTS.find((a) => a.id === agentId)?.name}_${availableModels.find((m) => m.id === modelId)?.name}_日期时间_赵启铭`}
          />
          <div className="confirm-block">
            <div className="confirm-block-head"><h4>案例清单</h4><span>{selectedCases.length} 条</span></div>
            {CASES.filter((item) => selectedCases.includes(item.id)).map((item, index) => (
              <div className="confirm-case" key={item.id}>
                <span>{String(index + 1).padStart(2, '0')}</span>
                <div><b>{item.name}</b><small>{item.code} · {item.category} · {item.difficulty}难度</small></div>
              </div>
            ))}
          </div>
        </div>
        <aside className="confirm-aside">
          <h4>配置摘要</h4>
          <div className="summary-picked">
            <span><RobotOutlined /></span>
            <div><small>Agent</small><b>{AGENTS.find((a) => a.id === agentId)?.name}</b><em>{AGENTS.find((a) => a.id === agentId)?.version}</em></div>
          </div>
          <div className="summary-picked">
            <span><DatabaseOutlined /></span>
            <div><small>测评模型</small><b>{availableModels.find((m) => m.id === modelId)?.name}</b><em>{availableModels.find((m) => m.id === modelId)?.version}</em></div>
          </div>
          <div className="queue-tip"><ClockCircleOutlined /><p><b>串行队列执行</b><br />预计耗时约 {selectedCases.length * 6}–{selectedCases.length * 10} 分钟</p></div>
        </aside>
      </div>
    </div>,
  ];

  return (
    <div className="page-stack">
      <div className="wizard-steps surface-card">
        <Steps
          current={step}
          items={[
            { title: '选择 Agent', description: AGENTS.find((a) => a.id === agentId)?.name },
            { title: '选择模型', description: step > 0 ? availableModels.find((m) => m.id === modelId)?.name : '待选择' },
            { title: '选择案例', description: step > 1 ? `${selectedCases.length} 条` : '待选择' },
            { title: '确认发起', description: '检查配置' },
          ]}
        />
      </div>
      <section className="surface-card wizard-card">{stepContent[step]}</section>
      <div className="wizard-actions">
        <Button size="large" disabled={step === 0} icon={<ArrowLeftOutlined />} onClick={() => setStep(step - 1)}>上一步</Button>
        {step < 3 ? (
          <Button type="primary" size="large" onClick={() => setStep(step + 1)} disabled={step === 2 && selectedCases.length === 0}>
            下一步 <ArrowRightOutlined />
          </Button>
        ) : (
          <Button type="primary" size="large" icon={<RocketOutlined />} onClick={() => onLaunch({ agentId, modelId, selectedCases, taskName })}>
            发起测评
          </Button>
        )}
      </div>
    </div>
  );
}

function TaskTable({ tasks, onOpenTask, compact = false }) {
  const columns = [
    {
      title: '任务名称',
      dataIndex: 'name',
      render: (_, task) => (
        <button className="task-link" onClick={() => onOpenTask(task)}>
          <span className="task-icon"><ExperimentOutlined /></span>
          <span><b>{task.name}</b><small>{task.id}</small></span>
        </button>
      ),
    },
    {
      title: '测评对象',
      key: 'target',
      width: 220,
      render: (_, task) => <div className="target-cell"><b>{AGENTS.find((a) => a.id === task.agentId)?.name}</b><small>{MODELS.find((m) => m.id === task.modelId)?.name}</small></div>,
    },
    {
      title: '案例',
      key: 'cases',
      width: 90,
      render: (_, task) => `${task.runs.length} 条`,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (value) => <TaskStatusTag status={value} />,
    },
    {
      title: '评分',
      dataIndex: 'scoringStatus',
      width: 100,
      render: (value) => <ScoreStatusTag status={value} />,
    },
    { title: '发起人', dataIndex: 'creator', width: 100 },
    { title: '发起时间', dataIndex: 'createdAt', width: 150 },
    {
      title: '',
      width: 48,
      render: (_, task) => <Button type="text" icon={<ArrowRightOutlined />} onClick={() => onOpenTask(task)} />,
    },
  ];
  return (
    <Table
      dataSource={tasks}
      columns={compact ? columns.filter((_, index) => ![4, 5].includes(index)) : columns}
      rowKey="id"
      pagination={false}
      scroll={{ x: compact ? 760 : 1050 }}
    />
  );
}

function RecordsPage({ tasks, onOpenTask }) {
  const [search, setSearch] = useState('');
  const filtered = tasks.filter((task) => task.name.toLowerCase().includes(search.toLowerCase()));
  return (
    <div className="page-stack">
      <section className="metrics-grid records-metrics">
        <MetricCard label="全部记录" value={tasks.length + 21} note="系统历史累计" tone="green" icon={<HistoryOutlined />} />
        <MetricCard label="已完成" value={tasks.filter((t) => t.status === 'completed').length + 17} note="完成率 91.7%" tone="blue" icon={<CheckCircleFilled />} />
        <MetricCard label="执行中" value="2" note="7 条案例正在排队" tone="orange" icon={<ThunderboltOutlined />} />
        <MetricCard label="平均得分" value="84.6" note="最近 30 天" tone="purple" icon={<BarChartOutlined />} />
      </section>
      <section className="surface-card table-card">
        <div className="section-head">
          <div><h3>历史测评</h3><p>按创建时间倒序排列，可复用配置重新发起</p></div>
          <Button icon={<ExportOutlined />}>导出记录</Button>
        </div>
        <div className="filter-bar">
          <Input prefix={<SearchOutlined />} value={search} onChange={(event) => setSearch(event.target.value)} placeholder="搜索任务名称" allowClear className="search-input" />
          <Select defaultValue="all" options={[{ value: 'all', label: '全部 Agent' }, ...AGENTS.map((a) => ({ value: a.id, label: a.name }))]} className="filter-select" />
          <Select defaultValue="all" options={[{ value: 'all', label: '全部状态' }, { value: 'completed', label: '已完成' }, { value: 'running', label: '执行中' }]} className="filter-select" />
          <Button icon={<ClockCircleOutlined />}>时间范围</Button>
          <span className="filter-count">共 {filtered.length + 21} 条记录</span>
        </div>
        <TaskTable tasks={filtered} onOpenTask={onOpenTask} />
      </section>
    </div>
  );
}

function ExecutionPage({ tasks, onOpenTask }) {
  const running = {
    id: 'T-20260723-04',
    name: 'Pi Agent_Pro-3.0_202607231022_赵启铭',
    agentId: 'pi-agent',
    modelId: 'm-pro',
    creator: '赵启铭',
    createdAt: '2026-07-23 10:22',
    status: 'running',
    scoringStatus: 'idle',
    runs: CASES.slice(0, 5).map((item, index) => ({
      caseId: item.id,
      status: index < 2 ? 'success' : index === 2 ? 'running' : 'queued',
      durationMs: index < 2 ? 156000 + index * 53000 : 0,
      tokensIn: index < 2 ? 18240 + index * 5200 : 0,
      tokensOut: index < 2 ? 4460 + index * 860 : 0,
    })),
  };
  const allTasks = [running, ...tasks];
  return (
    <div className="page-stack">
      <section className="running-banner">
        <div className="running-pulse"><span /></div>
        <div className="running-copy">
          <small>当前执行中</small>
          <h3>{running.name}</h3>
          <p>正在执行：{CASES[2].name} · 已完成 2 / 5</p>
        </div>
        <div className="running-progress">
          <div><span>整体进度</span><b>40%</b></div>
          <Progress percent={40} showInfo={false} strokeColor="#fff" trailColor="rgba(255,255,255,.18)" />
        </div>
        <Button ghost onClick={() => onOpenTask(running)}>进入监控 <ArrowRightOutlined /></Button>
      </section>
      <section className="surface-card table-card">
        <div className="section-head">
          <div><h3>全部任务</h3><p>统一查看执行队列与评分进度</p></div>
          <Space><Button icon={<ReloadOutlined />}>刷新</Button><Button type="primary" icon={<PlusOutlined />}>创建测评</Button></Space>
        </div>
        <div className="filter-bar">
          <Input prefix={<SearchOutlined />} placeholder="搜索任务" className="search-input" />
          <Radio.Group defaultValue="all" optionType="button" buttonStyle="solid" options={[{ label: '全部', value: 'all' }, { label: '执行中', value: 'running' }, { label: '已完成', value: 'done' }]} />
        </div>
        <TaskTable tasks={allTasks} onOpenTask={onOpenTask} />
      </section>
    </div>
  );
}

function TaskDetail({ task, onBack }) {
  const [selectedRun, setSelectedRun] = useState(null);
  const [scoreModal, setScoreModal] = useState(false);
  const isRunning = task.status === 'running';
  const success = task.runs.filter((run) => run.status === 'success').length;
  const failed = task.runs.filter((run) => run.status === 'failed').length;
  const completed = success + failed;
  const progress = Math.round((completed / task.runs.length) * 100);

  return (
    <div className="page-stack task-detail-page">
      <button className="back-link" onClick={onBack}><ArrowLeftOutlined /> 返回任务列表</button>
      <section className="task-detail-head surface-card">
        <div className="task-detail-title">
          <div className="task-icon large"><ExperimentOutlined /></div>
          <div>
            <Space><TaskStatusTag status={task.status} /><ScoreStatusTag status={task.scoringStatus} /></Space>
            <h2>{task.name}</h2>
            <p>{task.id} · 由 {task.creator} 发起于 {task.createdAt}</p>
          </div>
        </div>
        <Space wrap>
          <Button icon={<ReloadOutlined />}>复用配置</Button>
          {!isRunning && <Button type="primary" icon={<BarChartOutlined />} onClick={() => setScoreModal(true)}>发起自动评分</Button>}
          <Dropdown menu={{ items: [{ key: 'export', icon: <ExportOutlined />, label: '导出评分明细' }, { key: 'cancel', icon: <DeleteOutlined />, label: '取消任务', danger: true }] }}>
            <Button icon={<MoreOutlined />} />
          </Dropdown>
        </Space>
      </section>

      <section className="task-detail-grid">
        <div className="surface-card progress-panel">
          <div className="section-head"><div><h3>{isRunning ? '执行进度' : '执行结果'}</h3><p>串行队列 · 实时汇总</p></div><strong>{progress}%</strong></div>
          <Progress percent={progress} showInfo={false} strokeColor="#4f7a5b" />
          <div className="progress-stats">
            <div><span>总案例</span><b>{task.runs.length}</b></div>
            <div><span className="dot success" />成功<b>{success}</b></div>
            <div><span className="dot danger" />失败<b>{failed}</b></div>
            <div><span className="dot warning" />队列中<b>{task.runs.filter((r) => r.status === 'queued').length}</b></div>
          </div>
        </div>
        <div className="surface-card task-config-panel">
          <div><span>Agent</span><b>{AGENTS.find((a) => a.id === task.agentId)?.name}</b><small>{AGENTS.find((a) => a.id === task.agentId)?.version}</small></div>
          <div><span>测评模型</span><b>{MODELS.find((m) => m.id === task.modelId)?.name}</b><small>{MODELS.find((m) => m.id === task.modelId)?.tier}</small></div>
          <div><span>评分标准</span><b>{task.standardVersion || 'v2.0'}</b><small>通用评分标准</small></div>
        </div>
      </section>

      <section className="surface-card table-card">
        <div className="section-head">
          <div><h3>案例执行队列</h3><p>点击案例查看完整执行轨迹、日志和 Token 消耗</p></div>
          <Space><Select defaultValue="all" options={[{ value: 'all', label: '全部状态' }, { value: 'success', label: '成功' }, { value: 'failed', label: '失败' }]} /><Button icon={<PlusOutlined />}>添加案例</Button></Space>
        </div>
        <Table
          dataSource={task.runs}
          rowKey="caseId"
          pagination={false}
          onRow={(run) => ({ onClick: () => setSelectedRun(run), className: 'clickable-row' })}
          columns={[
            {
              title: '案例',
              dataIndex: 'caseId',
              render: (id) => {
                const item = CASES.find((c) => c.id === id);
                return <div className="simple-case"><b>{item?.name || id}</b><small>{item?.code} · {item?.category}</small></div>;
              },
            },
            { title: '状态', dataIndex: 'status', width: 110, render: (value) => <RunStatusTag status={value} /> },
            { title: '轮次', dataIndex: 'rounds', width: 80, render: (value) => value ? `${value} 轮` : '—' },
            { title: 'Token', width: 140, render: (_, run) => run.tokensIn ? `${((run.tokensIn + run.tokensOut) / 1000).toFixed(1)}k` : '—' },
            { title: '耗时', dataIndex: 'durationMs', width: 110, render: (value) => value ? `${Math.floor(value / 60000)}m ${Math.floor((value % 60000) / 1000)}s` : '—' },
            { title: '评分', width: 100, render: (_, run) => run.score ? `${Math.round(Object.values(run.score.dims).reduce((a, b) => a + b, 0) / Object.values(run.score.dims).length)} 分` : '—' },
            { title: '', width: 56, render: () => <Button type="text" icon={<ArrowRightOutlined />} /> },
          ]}
        />
      </section>
      <RunDrawer run={selectedRun} onClose={() => setSelectedRun(null)} />
      <ScoringModal open={scoreModal} onClose={() => setScoreModal(false)} />
    </div>
  );
}

function RunDrawer({ run, onClose }) {
  const item = CASES.find((c) => c.id === run?.caseId);
  const trajectory = run?.trajectory || [
    { role: 'user', time: '10:28:02', content: item?.prompt },
    { role: 'agent', time: '10:28:08', content: `收到任务，我将先分析 ${item?.repo} 的仓库结构并定位相关模块。` },
    { role: 'tool', time: '10:28:19', title: '执行命令', content: `$ git clone ${item?.repo}\n$ rg "theme" src/\nsrc/theme.js\nsrc/App.jsx` },
    { role: 'agent', time: '10:29:04', content: '已完成上下文分析，正在修改目标文件并补充测试。' },
  ];
  return (
    <Drawer open={Boolean(run)} onClose={onClose} width={720} title={item?.name || '执行详情'}>
      {run && (
        <div className="drawer-stack">
          <div className="run-overview">
            <div><span>执行状态</span><RunStatusTag status={run.status} /></div>
            <div><span>执行轮次</span><b>{run.rounds || 4} 轮</b></div>
            <div><span>输入 Token</span><b>{(run.tokensIn || 18240).toLocaleString()}</b></div>
            <div><span>输出 Token</span><b>{(run.tokensOut || 4460).toLocaleString()}</b></div>
          </div>
          <Tabs
            items={[
              {
                key: 'trajectory',
                label: '执行轨迹',
                children: (
                  <div className="trajectory">
                    {trajectory.map((entry, index) => entry.role === 'tool' ? (
                      <div className="trajectory-tool" key={index}>
                        <div><CodeOutlined /> {entry.title}<span>{entry.time}</span></div>
                        <pre>{entry.content}</pre>
                      </div>
                    ) : (
                      <div className={`trajectory-message ${entry.role}`} key={index}>
                        <Avatar icon={entry.role === 'user' ? <UserOutlined /> : <RobotOutlined />} />
                        <div><div><b>{entry.role === 'user' ? '用户' : 'Agent'}</b><span>{entry.time}</span></div><p>{entry.content}</p></div>
                      </div>
                    ))}
                    {run.status === 'running' && <div className="thinking"><span /><span /><span /> Agent 正在继续执行</div>}
                  </div>
                ),
              },
              { key: 'info', label: '案例信息', children: <div className="prompt-box">{item?.prompt}</div> },
              { key: 'log', label: '错误日志', children: run.error ? <pre className="error-log">{run.error.log}</pre> : <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="本次执行无错误日志" /> },
              { key: 'score', label: '评分详情', children: run.score ? <ScoreDetails score={run.score} /> : <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="该案例暂未评分" /> },
            ]}
          />
        </div>
      )}
    </Drawer>
  );
}

function ScoreDetails({ score }) {
  return (
    <div className="score-details">
      {Object.entries(score.dims).map(([key, value]) => {
        const dimension = SCORING_STANDARDS[1].dimensions.find((item) => item.key === key);
        return (
          <div className="score-dimension" key={key}>
            <div><b>{dimension?.label || key}</b><strong>{value}</strong></div>
            <Progress percent={value} showInfo={false} strokeColor="#4f7a5b" />
            <p>{score.comments[key]}</p>
          </div>
        );
      })}
      <div className="analysis-box"><b>模型分析与建议</b><p>{score.analysis}</p></div>
    </div>
  );
}

function ScoringModal({ open, onClose }) {
  return (
    <Modal
      open={open}
      onCancel={onClose}
      title="发起自动评分"
      okText="开始评分"
      onOk={() => { message.success('评分任务已进入队列（Mock）'); onClose(); }}
    >
      <div className="scoring-form">
        <label>评分模型</label>
        <Radio.Group defaultValue="judge-pro">
          {MODELS.filter((m) => m.scoring).map((model) => (
            <Radio.Button value={model.id} key={model.id}>{model.name}</Radio.Button>
          ))}
        </Radio.Group>
        <label>评分标准</label>
        <Select defaultValue="v2.0" options={SCORING_STANDARDS.map((s) => ({ value: s.version, label: `${s.version} · ${s.note}` }))} />
        <div className="queue-tip"><BarChartOutlined /><p><b>按案例进入评分队列</b><br />评分模型会基于标准答案逐条输出维度得分、评语和建议。</p></div>
      </div>
    </Modal>
  );
}

function SettingsPage() {
  const agentColumns = [
    { title: 'Agent', dataIndex: 'name', render: (_, agent) => <div className="agent-cell"><span><RobotOutlined /></span><div><b>{agent.name}</b><small>{agent.vendor}</small></div></div> },
    { title: '版本', dataIndex: 'version', width: 130 },
    { title: '状态', dataIndex: 'status', width: 120, render: () => <Badge status="success" text="已启用" /> },
    { title: '说明', dataIndex: 'desc', ellipsis: true },
    { title: '', width: 100, render: () => <Button type="text" icon={<EditOutlined />}>编辑</Button> },
  ];
  const modelColumns = [
    { title: '模型', dataIndex: 'name', render: (_, model) => <div className="agent-cell"><span><DatabaseOutlined /></span><div><b>{model.name}</b><small>{model.provider}</small></div></div> },
    { title: '类型', dataIndex: 'tier', width: 100, render: (value) => <Tag className="soft-tag">{value}</Tag> },
    { title: '版本', dataIndex: 'version', width: 120 },
    { title: '用途', dataIndex: 'scoring', width: 120, render: (value) => value ? '评分模型' : '测评模型' },
    { title: '状态', dataIndex: 'enabled', width: 110, render: (value) => <Badge status={value ? 'success' : 'default'} text={value ? '已启用' : '未启用'} /> },
    { title: '', width: 100, render: () => <Button type="text" icon={<EditOutlined />}>编辑</Button> },
  ];
  return (
    <div className="page-stack">
      <section className="surface-card settings-card">
        <Tabs
          size="large"
          items={[
            {
              key: 'agents',
              label: <span><RobotOutlined /> Agent 管理</span>,
              children: (
                <>
                  <div className="section-head"><div><h3>参测 Agent</h3><p>一期固定支持 Pi Agent、DevAgent CLI 与 OpenCode</p></div><Button icon={<PlusOutlined />}>接入 Agent</Button></div>
                  <Table dataSource={AGENTS} columns={agentColumns} rowKey="id" pagination={false} scroll={{ x: 760 }} />
                </>
              ),
            },
            {
              key: 'models',
              label: <span><DatabaseOutlined /> 模型配置</span>,
              children: (
                <>
                  <div className="section-head"><div><h3>模型列表</h3><p>配置测评模型与独立评分模型</p></div><Button type="primary" icon={<PlusOutlined />}>新增模型</Button></div>
                  <Table dataSource={MODELS} columns={modelColumns} rowKey="id" pagination={false} scroll={{ x: 850 }} />
                </>
              ),
            },
            {
              key: 'standards',
              label: <span><FileSearchOutlined /> 评分标准</span>,
              children: (
                <div className="standards-grid">
                  {SCORING_STANDARDS.map((standard) => (
                    <div className={`standard-card ${standard.current ? 'current' : ''}`} key={standard.id}>
                      <div><span>{standard.version}</span>{standard.current && <Tag color="success">当前版本</Tag>}</div>
                      <p>{standard.note}</p>
                      {standard.dimensions.map((d) => <div className="standard-dim" key={d.key}><span>{d.label}</span><b>{d.weight}%</b></div>)}
                      <small>更新于 {standard.updatedAt}</small>
                    </div>
                  ))}
                </div>
              ),
            },
          ]}
        />
      </section>
    </div>
  );
}

function App() {
  const [collapsed, setCollapsed] = useState(false);
  const [mobileNav, setMobileNav] = useState(false);
  const [activePage, setActivePage] = useState('dashboard');
  const [caseDrawer, setCaseDrawer] = useState(null);
  const [taskDetail, setTaskDetail] = useState(null);
  const tasks = useMemo(() => seedTasks(), []);

  const navigate = (key) => {
    setTaskDetail(null);
    setActivePage(key);
    setMobileNav(false);
  };

  const openTask = (task) => {
    setTaskDetail(task);
    setActivePage('execution');
  };

  const launchTask = () => {
    message.success('测评任务已创建，正在进入执行队列');
    openTask({
      id: 'T-20260723-05',
      name: 'Pi Agent_Pro-3.0_202607231530_赵启铭',
      agentId: 'pi-agent',
      modelId: 'm-pro',
      creator: '赵启铭',
      createdAt: '2026-07-23 15:30',
      status: 'running',
      scoringStatus: 'idle',
      runs: CASES.slice(0, 4).map((item, index) => ({
        caseId: item.id,
        status: index === 0 ? 'running' : 'queued',
        durationMs: 0,
        tokensIn: 0,
        tokensOut: 0,
      })),
    });
  };

  const content = taskDetail ? (
    <TaskDetail task={taskDetail} onBack={() => setTaskDetail(null)} />
  ) : {
    dashboard: <Dashboard tasks={tasks} onNavigate={navigate} onOpenTask={openTask} />,
    create: <CreateTask onLaunch={launchTask} />,
    cases: <CasesPage onSelectCase={setCaseDrawer} />,
    execution: <ExecutionPage tasks={tasks} onOpenTask={openTask} />,
    records: <RecordsPage tasks={tasks} onOpenTask={openTask} />,
    settings: <SettingsPage />,
  }[activePage];

  const [pageTitle, pageSubtitle] = taskDetail
    ? ['任务详情', '执行监控、轨迹追溯与评分处理']
    : pageMeta[activePage];

  return (
    <ConfigProvider
      theme={{
        token: {
          colorPrimary: '#4f7a5b',
          colorInfo: '#4f7a5b',
          colorSuccess: '#4f7a5b',
          colorWarning: '#c98a2e',
          colorError: '#c45448',
          colorText: '#292c29',
          colorTextSecondary: '#6f746e',
          colorBgBase: '#f5f3ee',
          colorBgContainer: '#fffdf9',
          colorBorder: '#e5e1d8',
          borderRadius: 10,
          borderRadiusLG: 16,
          fontFamily: '"Inter", "PingFang SC", "Microsoft YaHei", sans-serif',
          boxShadowSecondary: '0 18px 50px rgba(35, 43, 37, 0.12)',
        },
        components: {
          Button: { controlHeight: 36, fontWeight: 600 },
          Menu: { itemBorderRadius: 10, itemHeight: 46, itemMarginInline: 12 },
          Table: { headerBg: '#f7f5f0', headerColor: '#6d726d', rowHoverBg: '#f8f7f2' },
          Tabs: { itemSelectedColor: '#3f684a', inkBarColor: '#4f7a5b' },
        },
      }}
    >
      <Layout className="app-shell">
        <Sider
          width={248}
          collapsedWidth={76}
          collapsed={collapsed}
          className={`app-sidebar ${mobileNav ? 'mobile-open' : ''}`}
          trigger={null}
        >
          <button className="brand" onClick={() => navigate('dashboard')}>
            <span className="brand-mark"><span /></span>
            {!collapsed && <span><b>Agent Eval</b><small>智能化测评系统</small></span>}
          </button>
          <div className="workspace-label">{collapsed ? 'W' : 'WORKSPACE'}</div>
          <Menu mode="inline" selectedKeys={[activePage]} items={navItems} onClick={({ key }) => navigate(key)} />
          <div className="sidebar-bottom">
            {!collapsed && (
              <div className="system-health">
                <span><i /> 系统运行正常</span>
                <small>最后检查 1 分钟前</small>
              </div>
            )}
            <button className="collapse-btn" onClick={() => setCollapsed(!collapsed)}>
              {collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
              {!collapsed && '收起导航'}
            </button>
          </div>
        </Sider>
        {mobileNav && <button aria-label="关闭导航" className="mobile-mask" onClick={() => setMobileNav(false)} />}
        <Layout>
          <Header className="top-header">
            <div className="header-title">
              <Button className="mobile-menu" type="text" icon={<MenuUnfoldOutlined />} onClick={() => setMobileNav(true)} />
              <div><h1>{pageTitle}</h1><p>{pageSubtitle}</p></div>
            </div>
            <div className="header-actions">
              <Tooltip title="全局搜索"><Button type="text" shape="circle" icon={<SearchOutlined />} /></Tooltip>
              <Badge dot offset={[-4, 5]}><Button type="text" shape="circle" icon={<BellOutlined />} /></Badge>
              <span className="header-separator" />
              <Dropdown
                menu={{ items: [{ key: 'profile', icon: <UserOutlined />, label: '个人信息' }, { key: 'logout', label: '退出登录' }] }}
              >
                <button className="user-menu">
                  <Avatar>赵</Avatar>
                  <span><b>赵启铭</b><small>研发效能中心</small></span>
                  <DownOutlined />
                </button>
              </Dropdown>
            </div>
          </Header>
          <Content className="app-content">{content}</Content>
        </Layout>
      </Layout>
      <CaseDrawer item={caseDrawer} onClose={() => setCaseDrawer(null)} />
    </ConfigProvider>
  );
}

export default App;
