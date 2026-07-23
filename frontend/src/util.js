// ===== 通用工具 =====

export const pad2 = (n) => String(n).padStart(2, '0');

export function fmtDateTime(d) {
  const t = d instanceof Date ? d : new Date(d);
  return `${t.getFullYear()}-${pad2(t.getMonth() + 1)}-${pad2(t.getDate())} ${pad2(t.getHours())}:${pad2(t.getMinutes())}`;
}

export function fmtCompact(d) {
  const t = d instanceof Date ? d : new Date(d);
  return `${t.getFullYear()}${pad2(t.getMonth() + 1)}${pad2(t.getDate())}${pad2(t.getHours())}${pad2(t.getMinutes())}`;
}

export function fmtDur(ms) {
  if (ms == null) return '-';
  const s = Math.round(ms / 1000);
  if (s < 60) return `${s}s`;
  const m = Math.floor(s / 60);
  if (m < 60) return `${m}m${pad2(s % 60)}s`;
  return `${Math.floor(m / 60)}h${pad2(m % 60)}m`;
}

export function fmtTokens(n) {
  if (n == null) return '-';
  if (n >= 10000) return (n / 10000).toFixed(1) + 'w';
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k';
  return String(n);
}

export function caseScore(run, dimensions) {
  if (!run?.score) return null;
  let sum = 0;
  let w = 0;
  dimensions.forEach((d) => {
    const v = run.score.dims[d.key];
    if (typeof v === 'number') {
      sum += v * d.weight;
      w += d.weight;
    }
  });
  return w ? Math.round((sum / w) * 10) / 10 : null;
}

export function downloadCSV(filename, rows) {
  const csv = '﻿' + rows.map((r) => r.map(csvCell).join(',')).join('\r\n');
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}

function csvCell(v) {
  const s = v == null ? '' : String(v);
  return /[",\r\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
}
