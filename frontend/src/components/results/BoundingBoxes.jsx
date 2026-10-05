export default function BoundingBoxes({ pageSize, boxes }) {
  return (
    <svg viewBox={`0 0 ${pageSize.width} ${pageSize.height}`} preserveAspectRatio="none" aria-hidden="true">
      {boxes.map(({ bbox: [x1, y1, x2, y2], is_checked: isChecked }, boxIndex) => (
        <rect
          key={boxIndex}
          x={x1}
          y={y1}
          width={x2 - x1}
          height={y2 - y1}
          className={isChecked ? 'box box-checked' : 'box box-unchecked'}
        >
          <title>{isChecked ? 'Checked' : 'Unchecked'}</title>
        </rect>
      ))}
    </svg>
  )
}
