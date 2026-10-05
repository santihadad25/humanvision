import { useState } from 'react'
import { detectCheckboxes } from '../api/detectCheckboxes.js'

export function useDetection() {
  const [detection, setDetection] = useState(null)
  const [isDetecting, setIsDetecting] = useState(false)
  const [error, setError] = useState(null)

  function reset() {
    setDetection(null)
    setError(null)
  }

  async function detect(file) {
    reset()
    setIsDetecting(true)
    try {
      setDetection(await detectCheckboxes(file))
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setIsDetecting(false)
    }
  }

  return { detection, isDetecting, error, detect, reset }
}
