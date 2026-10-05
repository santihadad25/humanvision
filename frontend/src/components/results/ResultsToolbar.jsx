export default function ResultsToolbar({ showBoxes, onShowBoxesChange }) {
  return (
    <div className="toolbar">
      <div className="legend" aria-label="Legend">
        <span><i className="swatch swatch-checked" /> Checked</span>
        <span><i className="swatch swatch-unchecked" /> Unchecked</span>
      </div>
      <label className="switch">
        <input type="checkbox" checked={showBoxes} onChange={(event) => onShowBoxesChange(event.target.checked)} />
        <span className="switch-track" aria-hidden="true" />
        Show detected boxes
      </label>
    </div>
  )
}
