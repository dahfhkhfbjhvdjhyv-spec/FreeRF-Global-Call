using UnityEngine;

namespace FreeCity
{
    public sealed class ThirdPersonCamera : MonoBehaviour
    {
        [SerializeField] private Transform target;
        [SerializeField] private Vector3 offset = new Vector3(0f, 3.2f, -5.5f);
        [SerializeField] private float followSharpness = 10f;
        [SerializeField] private float lookHeight = 1.4f;

        private void LateUpdate()
        {
            if (target == null) return;
            Vector3 desired = target.position + target.rotation * offset;
            float blend = 1f - Mathf.Exp(-followSharpness * Time.deltaTime);
            transform.position = Vector3.Lerp(transform.position, desired, blend);
            Vector3 lookPoint = target.position + Vector3.up * lookHeight;
            transform.rotation = Quaternion.LookRotation(lookPoint - transform.position);
        }

        public void SetTarget(Transform newTarget) => target = newTarget;
    }
}
