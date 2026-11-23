using UnityEngine;

namespace FreeCity
{
    [RequireComponent(typeof(CharacterController))]
    public sealed class ThirdPersonPlayer : MonoBehaviour
    {
        [SerializeField] private float walkSpeed = 4.2f;
        [SerializeField] private float runSpeed = 7.0f;
        [SerializeField] private float turnSpeed = 540f;
        [SerializeField] private float gravity = -22f;
        [SerializeField] private Transform cameraTransform;

        private CharacterController controller;
        private float verticalVelocity;

        private void Awake()
        {
            controller = GetComponent<CharacterController>();
            if (cameraTransform == null && Camera.main != null)
                cameraTransform = Camera.main.transform;
        }

        private void Update()
        {
            Vector2 touchMove = FreeCityTouchInput.Move;
            float x = Mathf.Abs(touchMove.x) > 0.05f ? touchMove.x : Input.GetAxisRaw("Horizontal");
            float z = Mathf.Abs(touchMove.y) > 0.05f ? touchMove.y : Input.GetAxisRaw("Vertical");
            Vector3 input = new Vector3(x, 0f, z);
            input = Vector3.ClampMagnitude(input, 1f);

            Vector3 forward = cameraTransform != null
                ? Vector3.ProjectOnPlane(cameraTransform.forward, Vector3.up).normalized
                : Vector3.forward;
            Vector3 right = cameraTransform != null
                ? Vector3.ProjectOnPlane(cameraTransform.right, Vector3.up).normalized
                : Vector3.right;
            Vector3 move = (forward * input.z + right * input.x).normalized;

            if (move.sqrMagnitude > 0.001f)
            {
                Quaternion target = Quaternion.LookRotation(move, Vector3.up);
                transform.rotation = Quaternion.RotateTowards(
                    transform.rotation, target, turnSpeed * Time.deltaTime);
            }

            float speed = Input.GetKey(KeyCode.LeftShift) ? runSpeed : walkSpeed;
            if (controller.isGrounded && verticalVelocity < 0f)
                verticalVelocity = -2f;
            verticalVelocity += gravity * Time.deltaTime;

            Vector3 velocity = move * speed;
            velocity.y = verticalVelocity;
            controller.Move(velocity * Time.deltaTime);
        }
    }
}
