export function countCheckboxes(boxes) {
  const checked = boxes.filter((box) => box.is_checked).length
  return { total: boxes.length, checked, unchecked: boxes.length - checked }
}
