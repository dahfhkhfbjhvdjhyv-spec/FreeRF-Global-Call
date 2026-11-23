using System.Collections;
using UnityEngine;
using UnityEngine.SceneManagement;

namespace FreeCity
{
    // Additively streams city districts so the complete map is not loaded at once.
    public sealed class WorldZoneLoader : MonoBehaviour
    {
        [SerializeField] private string[] zoneSceneNames;
        [SerializeField] private float loadRadius = 450f;
        [SerializeField] private float checkInterval = 2f;
        [SerializeField] private Transform player;

        private readonly System.Collections.Generic.HashSet<string> loaded =
            new System.Collections.Generic.HashSet<string>();

        private IEnumerator Start()
        {
            while (player == null) yield return null;
            var wait = new WaitForSeconds(checkInterval);
            while (true)
            {
                foreach (string sceneName in zoneSceneNames)
                {
                    if (string.IsNullOrWhiteSpace(sceneName)) continue;
                    Scene scene = SceneManager.GetSceneByName(sceneName);
                    bool isLoaded = scene.IsValid() && scene.isLoaded;
                    // Zone anchor objects can be used later for distance-specific unloading.
                    if (!isLoaded && !loaded.Contains(sceneName))
                    {
                        loaded.Add(sceneName);
                        yield return SceneManager.LoadSceneAsync(sceneName, LoadSceneMode.Additive);
                    }
                }
                yield return wait;
            }
        }
    }
}
