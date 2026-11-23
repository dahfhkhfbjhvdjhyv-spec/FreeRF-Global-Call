using UnityEngine;
namespace FreeCity
{
    public sealed class FreeCityMission : MonoBehaviour
    {
        [SerializeField] private Transform destination;
        private bool completed;
        private GUIStyle style;
        private void OnGUI()
        {
            if (style == null) style = new GUIStyle(GUI.skin.box) { fontSize = 17, alignment = TextAnchor.MiddleLeft };
            GUI.Box(new Rect(12, 12, 310, 58), completed ? "MISSION COMPLETE  |  First Drive" : "MISSION: First Drive\nReach the glowing checkpoint", style);
        }
        private void Update()
        {
            if (destination != null && !completed) destination.Rotate(0, 40f * Time.deltaTime, 0, Space.World);
        }
        private void OnTriggerEnter(Collider other)
        {
            if (completed) return;
            if (other.GetComponent<ThirdPersonPlayer>() != null || other.GetComponentInParent<FreeCityVehicle>() != null)
            {
                completed = true;
                if (destination != null) destination.gameObject.SetActive(false);
                Debug.Log("FreeCity mission completed: First Drive");
            }
        }
        public void SetDestination(Transform value) { destination = value; }
    }
}