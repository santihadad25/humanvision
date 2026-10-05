import { countCheckboxes } from '../../lib/countCheckboxes.js'
import { CheckSquareIcon, LayersIcon, SquareIcon } from '../icons.jsx'

function Stat({ icon, label, value, tone }) {
  return (
    <div className={`stat stat-${tone}`}>
      <span className="stat-icon">{icon}</span>
      <span className="stat-value">{value}</span>
      <span className="stat-label">{label}</span>
    </div>
  )
}

export default function DetectionSummary({ detection }) {
  const { total, checked, unchecked } = countCheckboxes(detection.boxes)
  const pageCount = detection.pages.length

  return (
    <div className="stats" role="group" aria-label="Detection summary">
      <Stat icon={<LayersIcon />} label="Checkboxes found" value={total} tone="neutral" />
      <Stat icon={<CheckSquareIcon />} label="Checked" value={checked} tone="checked" />
      <Stat icon={<SquareIcon />} label="Unchecked" value={unchecked} tone="unchecked" />
      <Stat icon={<LayersIcon />} label={pageCount === 1 ? 'Page' : 'Pages'} value={pageCount} tone="neutral" />
    </div>
  )
}
