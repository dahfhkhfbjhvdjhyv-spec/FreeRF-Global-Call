using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;

namespace FreeCity
{
    // Zone scenes load near their named anchor and unload after the player leaves.
    public sealed class WorldZoneLoader : MonoBehaviour
    {
        [SerializeField] private string[] zoneSceneNames;
        [SerializeField] private float loadRadius = 450f;
        [SerializeField] private float unloadRadius = 600f;
        [SerializeField] private float checkInterval = 2f;
        [SerializeField] private Transform player;
        private readonly HashSet<string> loaded = new HashSet<string>();
        private readonly Dictionary<string, Transform> anchors = new Dictionary<string, Transform>();

        private IEnumerator Start()
        {
            foreach (string sceneName in zoneSceneNames)
            {
                GameObject anchor = GameObject.Find("ZoneAnchor_" + sceneName);
                if (anchor != null) anchors[sceneName] = anchor.transform;
            }
            var wait = new WaitForSeconds(checkInterval);
            while (player == null) yield return null;
            while (true)
            {
                foreach (string sceneName in zoneSceneNames)
                {
                    if (!anchors.TryGetValue(sceneName, out Transform anchor)) continue;
                    float distance = Vector3.Distance(player.position, anchor.position);
                    if (distance < loadRadius && !loaded.Contains(sceneName))
                    {
                        var op = SceneManager.LoadSceneAsync(sceneName, LoadSceneMode.Additive);
                        if (op != null) yield return op;
                        loaded.Add(sceneName);
                    }
                    else if (distance > unloadRadius && loaded.Contains(sceneName))
                    {
                        var op = SceneManager.UnloadSceneAsync(sceneName);
                        if (op != null) yield return op;
                        loaded.Remove(sceneName);
                    }
                }
                yield return wait;
            }
        }
    }
}