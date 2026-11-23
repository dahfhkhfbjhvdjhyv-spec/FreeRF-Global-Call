using UnityEngine;
namespace FreeCity
{
    // Lightweight sidewalk walkers; deliberately avoids NavMesh for a dependency-free mobile prototype.
    public sealed class FreeCityPedestrians : MonoBehaviour
    {
        [SerializeField] private int pedestrianCount = 12;
        [SerializeField] private float blockLength = 220f;
        [SerializeField] private float walkSpeed = 1.15f;
        private Transform[] people;
        private float[] phases;

        private void Start()
        {
            people = new Transform[pedestrianCount];
            phases = new float[pedestrianCount];
            for (int i = 0; i < pedestrianCount; i++)
            {
                GameObject person = GameObject.CreatePrimitive(PrimitiveType.Capsule);
                person.name = "Pedestrian_" + i;
                person.transform.localScale = new Vector3(0.55f, 0.9f, 0.55f);
                bool alongZ = (i % 2) == 0;
                float lane = (i % 4 < 2) ? -1f : 1f;
                person.transform.position = alongZ
                    ? new Vector3(lane * 11f, 0.9f, -blockLength * 0.5f + i * 17f)
                    : new Vector3(-blockLength * 0.5f + i * 17f, 0.9f, lane * 11f);
                person.transform.rotation = Quaternion.Euler(0f, alongZ ? 0f : 90f, 0f);
                Renderer renderer = person.GetComponent<Renderer>();
                Material material = new Material(Shader.Find("Standard"));
                material.color = Color.HSVToRGB((i * 0.13f + 0.05f) % 1f, 0.45f, 0.85f);
                renderer.sharedMaterial = material;
                Collider col = person.GetComponent<Collider>();
                if (col != null) Destroy(col);
                people[i] = person.transform;
                phases[i] = (i % 2 == 0) ? 1f : -1f;
            }
        }

        private void Update()
        {
            if (people == null) return;
            for (int i = 0; i < people.Length; i++)
            {
                Transform person = people[i];
                if (person == null) continue;
                bool alongZ = (i % 2) == 0;
                Vector3 direction = alongZ ? Vector3.forward : Vector3.right;
                person.position += direction * phases[i] * walkSpeed * Time.deltaTime;
                float coordinate = alongZ ? person.position.z : person.position.x;
                if (coordinate > blockLength * 0.5f) phases[i] = -1f;
                if (coordinate < -blockLength * 0.5f) phases[i] = 1f;
                person.rotation = Quaternion.LookRotation(direction * phases[i]);
            }
        }
    }
}