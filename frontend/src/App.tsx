import { useMemo, useState } from 'react';
import {
  Avatar,
  Badge,
  Button,
  ConfigProvider,
  Dropdown,
  Layout,
  Menu,
  Tooltip,
  message,
} from 'antd';
import {
  BellOutlined,
  BookOutlined,
  DownOutlined,
  HistoryOutlined,
  HomeOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  RocketOutlined,
  SearchOutlined,
  SettingOutlined,
  ThunderboltOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { seedTasks } from './mock/data';
import type { CreateTaskPayload, EvaluationCase, EvaluationTask } from './types';
import { DashboardPage } from './pages/DashboardPage';
import { CreateTaskPage } from './pages/CreateTaskPage';
import { CasesPage } from './pages/CasesPage';
import { ExecutionPage } from './pages/ExecutionPage';
import { RecordsPage } from './pages/RecordsPage';
import { SettingsPage } from './pages/SettingsPage';
import { TaskDetailPage } from './pages/TaskDetailPage';
import { CaseDrawer } from './components/cases/CaseDetail';
import './App.css';

const { Header, Sider, Content } = Layout;

type PageKey = 'dashboard' | 'create' | 'cases' | 'execution' | 'records' | 'settings';

const pageMeta: Record<PageKey, [string, string]> = {
  dashboard: ['工作台', '掌握测评进展，快速开始新一轮智能化评估'],
  create: ['创建测评', '选择 Agent、模型和案例，组装一次标准化测评'],
  cases: ['案例库', '管理可复用的真实研发场景与标准答案'],
  execution: ['执行中心', '监控任务队列、执行轨迹与自动评分'],
  records: ['测评记录', '查询、追溯并复用每一次历史测评'],
  settings: ['Agent 与模型', '管理参测 Agent、模型配置与评分标准'],
};

const navItems = [
  { key: 'dashboard', icon: <HomeOutlined />, label: '工作台' },
  { key: 'create', icon: <RocketOutlined />, label: '创建测评' },
  { key: 'cases', icon: <BookOutlined />, label: '案例库' },
  { key: 'execution', icon: <ThunderboltOutlined />, label: '执行中心' },
  { key: 'records', icon: <HistoryOutlined />, label: '测评记录' },
  { type: 'divider' as const },
  { key: 'settings', icon: <SettingOutlined />, label: 'Agent 与模型' },
];

function createTaskFromPayload(payload: CreateTaskPayload): EvaluationTask {
  const now = new Date();
  const stamp = now.toISOString().slice(0, 16).replace(/[-T:]/g, '');
  return {
    id: `T-${stamp}-05`,
    name: payload.taskName || `Agent测评_${stamp}_赵启铭`,
    agentId: payload.agentId,
    agentVersionId: payload.agentVersionId,
    modelId: payload.modelId,
    creator: '赵启铭',
    createdAt: now.toLocaleString('zh-CN', { hour12: false }).replaceAll('/', '-'),
    status: 'running',
    scoringStatus: 'idle',
    runs: payload.selectedCases.map((caseId, index) => ({
      caseId,
      status: index === 0 ? 'running' : 'queued',
      durationMs: 0,
      tokensIn: 0,
      tokensOut: 0,
    })),
  };
}

export default function App() {
  const [collapsed, setCollapsed] = useState(false);
  const [mobileNav, setMobileNav] = useState(false);
  const [activePage, setActivePage] = useState<PageKey>('dashboard');
  const [caseDrawer, setCaseDrawer] = useState<EvaluationCase | null>(null);
  const [taskDetail, setTaskDetail] = useState<EvaluationTask | null>(null);
  const tasks = useMemo(() => seedTasks(), []);

  const navigate = (key: string) => {
    setTaskDetail(null);
    setActivePage(key as PageKey);
    setMobileNav(false);
  };

  const openTask = (task: EvaluationTask) => {
    setTaskDetail(task);
    setActivePage('execution');
  };

  const launchTask = (payload: CreateTaskPayload) => {
    message.success('测评任务已创建，正在进入执行队列');
    openTask(createTaskFromPayload(payload));
  };

  const renderPage = () => {
    if (taskDetail) {
      return <TaskDetailPage task={taskDetail} onBack={() => setTaskDetail(null)} />;
    }
    switch (activePage) {
      case 'dashboard':
        return <DashboardPage tasks={tasks} onNavigate={navigate} onOpenTask={openTask} />;
      case 'create':
        return <CreateTaskPage onLaunch={launchTask} />;
      case 'cases':
        return <CasesPage onSelectCase={setCaseDrawer} />;
      case 'execution':
        return <ExecutionPage tasks={tasks} onOpenTask={openTask} onCreate={() => navigate('create')} />;
      case 'records':
        return <RecordsPage tasks={tasks} onOpenTask={openTask} />;
      case 'settings':
        return <SettingsPage />;
      default:
        return null;
    }
  };

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
              <Dropdown menu={{ items: [{ key: 'profile', icon: <UserOutlined />, label: '个人信息' }, { key: 'logout', label: '退出登录' }] }}>
                <button className="user-menu">
                  <Avatar>赵</Avatar>
                  <span><b>赵启铭</b><small>研发效能中心</small></span>
                  <DownOutlined />
                </button>
              </Dropdown>
            </div>
          </Header>
          <Content className="app-content">{renderPage()}</Content>
        </Layout>
      </Layout>
      <CaseDrawer item={caseDrawer} onClose={() => setCaseDrawer(null)} />
    </ConfigProvider>
  );
}
