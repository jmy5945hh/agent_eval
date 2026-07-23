import type { ReactNode } from 'react';

interface MetricCardProps {
  label: string;
  value: string | number;
  note: string;
  tone?: 'green' | 'blue' | 'orange' | 'purple';
  icon: ReactNode;
}

export function MetricCard({ label, value, note, tone = 'green', icon }: MetricCardProps) {
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

interface MiniDonutProps {
  value: number;
  label: string;
  color?: string;
}

export function MiniDonut({ value, label, color = '#4f7a5b' }: MiniDonutProps) {
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
