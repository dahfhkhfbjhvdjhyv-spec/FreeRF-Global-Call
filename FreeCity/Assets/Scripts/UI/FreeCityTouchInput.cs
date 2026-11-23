using UnityEngine;

namespace FreeCity
{
    // Simple built-in touch controls: left half steers, right half accelerates/brakes.
    // Keyboard controls remain available in the editor for rapid testing.
    public sealed class FreeCityTouchInput : MonoBehaviour
    {
        public static Vector2 Move { get; private set; }

        private void Update()
        {
            if (Input.touchCount == 0) { Move = Vector2.zero; return; }
            Vector2 value = Vector2.zero;
            foreach (Touch touch in Input.touches)
            {
                if (touch.position.x < Screen.width * 0.5f)
                {
                    value.x = Mathf.Clamp((touch.position.x / (Screen.width * 0.5f) - 0.5f) * 2f, -1f, 1f);
                    value.y = Mathf.Clamp((touch.position.y / Screen.height - 0.2f) * 1.4f, -1f, 1f);
                }
                else
                {
                    value.y = Mathf.Clamp((touch.position.y / Screen.height - 0.5f) * 2f, -1f, 1f);
                }
            }
            Move = Vector2.ClampMagnitude(value, 1f);
        }
    }
}
