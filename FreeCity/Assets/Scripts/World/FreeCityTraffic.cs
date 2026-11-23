using UnityEngine;
namespace FreeCity
{
    // Small, low-cost ambient traffic loop for the Android prototype.
    public sealed class FreeCityTraffic : MonoBehaviour
    {
        [SerializeField] private int vehicleCount = 6;
        [SerializeField] private float roadLength = 230f;
        [SerializeField] private float speed = 7f;
        private Transform[] cars;
        private void Start()
        {
            cars = new Transform[vehicleCount];
            for (int i = 0; i < vehicleCount; i++)
            {
                GameObject car = GameObject.CreatePrimitive(PrimitiveType.Cube);
                car.name = "AmbientTraffic_" + i;
                car.transform.localScale = new Vector3(1.7f, 1.05f, 3.2f);
                car.transform.position = new Vector3(i % 2 == 0 ? -3.2f : 3.2f, 0.55f,
                    -roadLength * 0.5f + i * roadLength / vehicleCount);
                car.transform.rotation = Quaternion.Euler(0, i % 2 == 0 ? 0 : 180, 0);
                Material m = new Material(Shader.Find("Standard"));
                m.color = Color.HSVToRGB((i * 0.17f) % 1f, 0.65f, 0.8f);
                car.GetComponent<Renderer>().sharedMaterial = m;
                Destroy(car.GetComponent<Collider>());
                cars[i] = car.transform;
            }
        }
        private void Update()
        {
            if (cars == null) return;
            for (int i = 0; i < cars.Length; i++)
            {
                Transform car = cars[i];
                float direction = i % 2 == 0 ? 1f : -1f;
                car.position += Vector3.forward * direction * speed * Time.deltaTime;
                if (car.position.z > roadLength * 0.5f) car.position = new Vector3(car.position.x, car.position.y, -roadLength * 0.5f);
                if (car.position.z < -roadLength * 0.5f) car.position = new Vector3(car.position.x, car.position.y, roadLength * 0.5f);
            }
        }
    }
}