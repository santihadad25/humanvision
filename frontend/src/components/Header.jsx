import { CheckSquareIcon } from './icons.jsx'

export default function Header() {
  return (
    <header className="app-header">
      <span className="app-logo">
        <CheckSquareIcon width={26} height={26} />
      </span>
      <div>
        <h1>Checkbox Detection</h1>
        <p>Upload an image or a PDF and see which checkboxes it has and which ones are checked.</p>
      </div>
    </header>
  )
}
