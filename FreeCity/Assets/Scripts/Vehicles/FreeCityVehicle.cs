using UnityEngine;

namespace FreeCity
{
    public sealed class FreeCityVehicle : MonoBehaviour
    {
        [SerializeField] private float acceleration = 18f;
        [SerializeField] private float reverseAcceleration = 9f;
        [SerializeField] private float maxSpeed = 28f;
        [SerializeField] private float steeringRate = 95f;
        [SerializeField] private Transform seatPoint;

        private float speed;
        private ThirdPersonPlayer nearbyPlayer;
        private ThirdPersonPlayer driver;
        private CharacterController driverController;

        private void Update()
        {
            if (driver == null)
            {
                if (nearbyPlayer != null && Input.GetKeyDown(KeyCode.E))
                    EnterVehicle();
                return;
            }

            float throttle = Input.GetAxisRaw("Vertical");
            float steer = Input.GetAxisRaw("Horizontal");
            float target = throttle >= 0f ? throttle * maxSpeed : throttle * maxSpeed * 0.45f;
            float rate = throttle >= 0f ? acceleration : reverseAcceleration;
            speed = Mathf.MoveTowards(speed, target, rate * Time.deltaTime);
            float steeringScale = Mathf.Clamp01(Mathf.Abs(speed) / 4f);
            transform.Rotate(0f, steer * steeringRate * steeringScale *
                Mathf.Sign(speed == 0f ? 1f : speed) * Time.deltaTime, 0f);
            transform.position += transform.forward * speed * Time.deltaTime;

            if (seatPoint != null)
                driver.transform.SetPositionAndRotation(seatPoint.position, transform.rotation);
            if (Camera.main != null)
            {
                Vector3 cameraTarget = transform.position - transform.forward * 7f + Vector3.up * 3.5f;
                Camera.main.transform.position = Vector3.Lerp(Camera.main.transform.position,
                    cameraTarget, 7f * Time.deltaTime);
                Camera.main.transform.LookAt(transform.position + Vector3.up * 1.2f);
            }
            if (Input.GetKeyDown(KeyCode.F))
                ExitVehicle();
        }

        private void OnTriggerEnter(Collider other)
        {
            var player = other.GetComponent<ThirdPersonPlayer>();
            if (player != null && driver == null) nearbyPlayer = player;
        }

        private void OnTriggerExit(Collider other)
        {
            var player = other.GetComponent<ThirdPersonPlayer>();
            if (player != null && player == nearbyPlayer) nearbyPlayer = null;
        }

        private void EnterVehicle()
        {
            driver = nearbyPlayer;
            if (driver == null) return;
            driverController = driver.GetComponent<CharacterController>();
            if (driverController != null) driverController.enabled = false;
            driver.gameObject.SetActive(false);
            if (seatPoint != null) driver.transform.position = seatPoint.position;
            nearbyPlayer = null;
        }

        private void ExitVehicle()
        {
            if (driver == null) return;
            driver.gameObject.SetActive(true);
            driver.transform.position = transform.position + transform.right * 2.5f + Vector3.up;
            if (driverController != null) driverController.enabled = true;
            driver = null;
            speed = 0f;
        }
    }
}
