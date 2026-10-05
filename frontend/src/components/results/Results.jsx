import { useState } from 'react'
import DetectionSummary from './DetectionSummary.jsx'
import EmptyResultNotice from './EmptyResultNotice.jsx'
import PageView from './PageView.jsx'
import ResultsToolbar from './ResultsToolbar.jsx'

export default function Results({ pagePreviewUrls, detection }) {
  const [showBoxes, setShowBoxes] = useState(true)

  if (pagePreviewUrls.length === 0 && !detection) return null

  const hasBoxes = detection?.boxes.length > 0

  return (
    <section className="results" aria-label="Results">
      {detection && <DetectionSummary detection={detection} />}
      {detection && !hasBoxes && <EmptyResultNotice />}
      {hasBoxes && <ResultsToolbar showBoxes={showBoxes} onShowBoxesChange={setShowBoxes} />}
      <div className="pages">
        {pagePreviewUrls.map((previewUrl, index) => {
          const pageNumber = index + 1
          return (
            <PageView
              key={pageNumber}
              pageNumber={pageNumber}
              previewUrl={previewUrl}
              pageSize={detection?.pages.find((page) => page.page === pageNumber)}
              boxes={detection?.boxes.filter((box) => box.page === pageNumber) ?? []}
              showBoxes={showBoxes}
            />
          )
        })}
      </div>
      {detection && (
        <details className="raw-json">
          <summary>Raw response</summary>
          <pre>{JSON.stringify(detection, null, 2)}</pre>
        </details>
      )}
    </section>
  )
}
