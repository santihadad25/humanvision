const UNREACHABLE_MESSAGE = 'The server could not be reached.'
const UNEXPECTED_RESPONSE_MESSAGE = 'The server answered with an unexpected response.'

export async function detectCheckboxes(file) {
  const body = new FormData()
  body.append('file', file)
  const response = await fetch('/api/v2/detect', { method: 'POST', body }).catch(() => {
    throw new Error(UNREACHABLE_MESSAGE)
  })
  const payload = await response.json().catch(() => null)
  if (!response.ok) {
    throw new Error(payload?.detail || `Request failed (${response.status})`)
  }
  if (!isDetectionResponse(payload)) {
    throw new Error(UNEXPECTED_RESPONSE_MESSAGE)
  }
  return payload
}

function isDetectionResponse(payload) {
  return Array.isArray(payload?.boxes) && Array.isArray(payload?.pages)
}
