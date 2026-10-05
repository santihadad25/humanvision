import Notice from '../Notice.jsx'

export default function EmptyResultNotice() {
  return (
    <Notice tone="info">
      <strong>No checkboxes were found.</strong>
      <p>
        If the file is a screenshot or a small image, the boxes may be too small to recognise: zoom in on the document
        before capturing it, so that each checkbox is at least about 15 pixels wide. Sharp, high-resolution scans and
        PDFs work best.
      </p>
    </Notice>
  )
}
