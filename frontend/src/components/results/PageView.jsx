import { countCheckboxes } from '../../lib/countCheckboxes.js'
import BoundingBoxes from './BoundingBoxes.jsx'

function PageCounts({ boxes }) {
  const { total, checked, unchecked } = countCheckboxes(boxes)
  return (
    <span className="page-card-counts">
      {total} {total === 1 ? 'checkbox' : 'checkboxes'}
      <span className="dot dot-checked" /> {checked} checked
      <span className="dot dot-unchecked" /> {unchecked} unchecked
    </span>
  )
}

export default function PageView({ pageNumber, previewUrl, pageSize, boxes, showBoxes }) {
  return (
    <article className="page-card">
      <header className="page-card-header">
        <h3>Page {pageNumber}</h3>
        {pageSize && <PageCounts boxes={boxes} />}
      </header>
      <div className="stage">
        <div className="page-image">
          <img src={previewUrl} alt={`Page ${pageNumber} of the uploaded document`} />
          {pageSize && showBoxes && <BoundingBoxes pageSize={pageSize} boxes={boxes} />}
        </div>
      </div>
    </article>
  )
}
