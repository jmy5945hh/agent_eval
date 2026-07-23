import type { ReactNode } from 'react';
import { CheckCircleFilled } from '@ant-design/icons';

interface SelectionCardProps {
  active: boolean;
  onClick: () => void;
  title: string;
  subtitle: string;
  description: string;
  meta: string;
  icon: ReactNode;
  compact?: boolean;
}

export function SelectionCard({
  active,
  onClick,
  title,
  subtitle,
  description,
  meta,
  icon,
  compact = false,
}: SelectionCardProps) {
  return (
    <button className={`selection-card ${active ? 'active' : ''} ${compact ? 'compact' : ''}`} onClick={onClick}>
      <span className="selection-check">{active ? <CheckCircleFilled /> : null}</span>
      <span className="selection-icon">{icon}</span>
      <span className="selection-title">{title}<small>{subtitle}</small></span>
      <span className="selection-desc">{description}</span>
      <span className="selection-meta">{meta}</span>
    </button>
  );
}
