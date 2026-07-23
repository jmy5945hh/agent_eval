import { Avatar, Drawer, Empty, Progress, Tabs } from 'antd';
import { CodeOutlined, RobotOutlined, UserOutlined } from '@ant-design/icons';
import { CASES, SCORING_STANDARDS } from '../../mock/data';
import type { CaseRun, RunScore, TrajectoryEntry } from '../../types';
import { RunStatusTag } from '../StatusTag';

function ScoreDetails({ score }: { score: RunScore }) {
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

export function RunDrawer({ run, onClose }: { run: CaseRun | null; onClose: () => void }) {
  const item = CASES.find((caseItem) => caseItem.id === run?.caseId);
  const fallbackTrajectory: TrajectoryEntry[] = [
    { role: 'user', time: '10:28:02', content: item?.prompt || '' },
    { role: 'agent', time: '10:28:08', content: `收到任务，我将先分析 ${item?.repo} 的仓库结构并定位相关模块。` },
    { role: 'tool', time: '10:28:19', title: '执行命令', content: `$ git clone ${item?.repo}\n$ rg "theme" src/\nsrc/theme.js\nsrc/App.jsx` },
    { role: 'agent', time: '10:29:04', content: '已完成上下文分析，正在修改目标文件并补充测试。' },
  ];
  const trajectory = run?.trajectory || fallbackTrajectory;

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
                      <div className="trajectory-tool" key={`${entry.time}-${index}`}>
                        <div><CodeOutlined /> {entry.title}<span>{entry.time}</span></div>
                        <pre>{entry.content}</pre>
                      </div>
                    ) : (
                      <div className={`trajectory-message ${entry.role}`} key={`${entry.time}-${index}`}>
                        <Avatar icon={entry.role === 'user' ? <UserOutlined /> : <RobotOutlined />} />
                        <div>
                          <div><b>{entry.role === 'user' ? '用户' : 'Agent'}</b><span>{entry.time}</span></div>
                          <p>{entry.content}</p>
                        </div>
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
