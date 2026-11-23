using UnityEngine;
namespace FreeCity
{
    public sealed class FreeCityVehicle : MonoBehaviour
    {
        [SerializeField] private float acceleration = 13f;
        [SerializeField] private float reverseAcceleration = 8f;
        [SerializeField] private float maxSpeed = 22f;
        [SerializeField] private float steeringRate = 82f;
        [SerializeField] private Transform seatPoint;
        private float speed;
        private ThirdPersonPlayer nearbyPlayer;
        private ThirdPersonPlayer driver;
        private CharacterController driverController;
        private ThirdPersonCamera followCamera;

        private void Awake()
        {
            if (Camera.main != null) followCamera = Camera.main.GetComponent<ThirdPersonCamera>();
        }
        private void Update()
        {
            if (driver == null)
            {
                if (nearbyPlayer != null && (FreeCityTouchInput.ConsumeEnter() || Input.GetKeyDown(KeyCode.E))) EnterVehicle();
                return;
            }
            float throttle = Mathf.Abs(FreeCityTouchInput.Throttle) > 0.01f ? FreeCityTouchInput.Throttle : Input.GetAxisRaw("Vertical");
            float steer = Mathf.Abs(FreeCityTouchInput.Steering) > 0.01f ? FreeCityTouchInput.Steering : Input.GetAxisRaw("Horizontal");
            float targetSpeed = throttle >= 0 ? throttle * maxSpeed : throttle * maxSpeed * 0.4f;
            speed = Mathf.MoveTowards(speed, targetSpeed, (throttle >= 0 ? acceleration : reverseAcceleration) * Time.deltaTime);
            float steeringScale = Mathf.Clamp01(Mathf.Abs(speed) / 3f);
            transform.Rotate(0, steer * steeringRate * steeringScale * Mathf.Sign(speed == 0 ? 1 : speed) * Time.deltaTime, 0);
            transform.position += transform.forward * speed * Time.deltaTime;
            if (seatPoint != null) driver.transform.SetPositionAndRotation(seatPoint.position, transform.rotation);
            if (FreeCityTouchInput.ConsumeExit() || Input.GetKeyDown(KeyCode.F)) ExitVehicle();
        }
        private void OnTriggerEnter(Collider other)
        {
            ThirdPersonPlayer player = other.GetComponent<ThirdPersonPlayer>();
            if (player != null && driver == null) nearbyPlayer = player;
        }
        private void OnTriggerExit(Collider other)
        {
            ThirdPersonPlayer player = other.GetComponent<ThirdPersonPlayer>();
            if (player != null && player == nearbyPlayer) nearbyPlayer = null;
        }
        private void EnterVehicle()
        {
            driver = nearbyPlayer;
            if (driver == null) return;
            driverController = driver.GetComponent<CharacterController>();
            if (driverController != null) driverController.enabled = false;
            driver.gameObject.SetActive(false);
            if (followCamera != null) followCamera.SetTarget(transform);
            nearbyPlayer = null;
        }
        private void ExitVehicle()
        {
            if (driver == null) return;
            ThirdPersonPlayer player = driver;
            player.gameObject.SetActive(true);
            player.transform.position = transform.position + transform.right * 2.5f + Vector3.up * 0.2f;
            if (driverController != null) driverController.enabled = true;
            if (followCamera != null) followCamera.SetTarget(player.transform);
            driver = null;
            speed = 0;
        }
    }
}