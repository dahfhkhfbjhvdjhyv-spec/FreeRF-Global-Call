using UnityEngine;

namespace FreeCity
{
    // Screen-space virtual controls; works without the legacy touch-drag gesture.
    public sealed class FreeCityTouchInput : MonoBehaviour
    {
        public static Vector2 Move { get; private set; }
        public static float Throttle { get; private set; }
        public static float Steering { get; private set; }
        private static bool enterPressed;
        private static bool exitPressed;

        public static bool ConsumeEnter() { bool v = enterPressed; enterPressed = false; return v; }
        public static bool ConsumeExit() { bool v = exitPressed; exitPressed = false; return v; }

        private void OnGUI()
        {
            float s = Mathf.Clamp(Screen.width / 720f, 0.8f, 1.5f);
            float b = 72f * s, p = 16f * s, bottom = Screen.height - b - p;
            GUI.skin.button.fontSize = Mathf.RoundToInt(22f * s);
            bool left = GUI.RepeatButton(new Rect(p, bottom - b, b, b), "LEFT");
            bool right = GUI.RepeatButton(new Rect(p + b * 2, bottom - b, b, b), "RIGHT");
            bool up = GUI.RepeatButton(new Rect(p + b, bottom - b * 2, b, b), "UP");
            bool down = GUI.RepeatButton(new Rect(p + b, bottom, b, b), "DOWN");
            Move = new Vector2((right ? 1 : 0) - (left ? 1 : 0), (up ? 1 : 0) - (down ? 1 : 0));
            float rx = Screen.width - p - b;
            bool gas = GUI.RepeatButton(new Rect(rx, bottom - b, b, b), "GAS");
            bool brake = GUI.RepeatButton(new Rect(rx, bottom, b, b), "BRAKE");
            bool steerL = GUI.RepeatButton(new Rect(rx - b * 2, bottom - b, b, b), "◀");
            bool steerR = GUI.RepeatButton(new Rect(rx - b, bottom - b, b, b), "▶");
            Throttle = (gas ? 1f : 0f) - (brake ? 1f : 0f);
            Steering = (steerR ? 1f : 0f) - (steerL ? 1f : 0f);
            float w = 110f * s;
            if (GUI.Button(new Rect(Screen.width - w - p, p, w, 46f * s), "ENTER")) enterPressed = true;
            if (GUI.Button(new Rect(Screen.width - w - p, p + 52f * s, w, 46f * s), "EXIT")) exitPressed = true;
        }
    }
}