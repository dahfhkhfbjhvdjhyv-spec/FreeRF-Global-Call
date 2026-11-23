using UnityEditor;
using UnityEditor.SceneManagement;
using UnityEngine;
using UnityEngine.SceneManagement;

namespace FreeCity.Editor
{
    public static class FreeCityBootstrap
    {
        [MenuItem("FreeCity/Generate Prototype City Scene")]
        public static void Generate()
        {
            var scene = EditorSceneManager.NewScene(NewSceneSetup.DefaultGameObjects, NewSceneMode.Single);
            scene.name = "FreeCity_Prototype";

            Material ground = MakeMaterial("Ground", new Color(0.18f, 0.28f, 0.19f));
            Material road = MakeMaterial("Road", new Color(0.10f, 0.11f, 0.13f));
            Material buildingA = MakeMaterial("Concrete", new Color(0.42f, 0.46f, 0.50f));
            Material buildingB = MakeMaterial("Sandstone", new Color(0.62f, 0.52f, 0.39f));

            CreateCube("Terrain", new Vector3(0, -0.6f, 0), new Vector3(500, 1, 500), ground);
            CreateCube("MainRoad_NS", new Vector3(0, -0.05f, 0), new Vector3(16, 0.1f, 500), road);
            CreateCube("MainRoad_EW", new Vector3(0, -0.04f, 0), new Vector3(500, 0.1f, 16), road);

            for (int x = -4; x <= 4; x++)
            for (int z = -4; z <= 4; z++)
            {
                if (Mathf.Abs(x) <= 1 || Mathf.Abs(z) <= 1) continue;
                float h = 10f + Mathf.Abs((x * 7 + z * 3) % 19);
                var mat = ((x + z) & 1) == 0 ? buildingA : buildingB;
                CreateCube("Building_" + x + "_" + z,
                    new Vector3(x * 42f, h * 0.5f, z * 42f),
                    new Vector3(25f, h, 25f), mat);
            }

            var player = GameObject.CreatePrimitive(PrimitiveType.Capsule);
            player.name = "Player";
            player.transform.position = new Vector3(0, 1.2f, -12f);
            Object.DestroyImmediate(player.GetComponent<CapsuleCollider>());
            player.AddComponent<CharacterController>();
            player.AddComponent<ThirdPersonPlayer>();

            var cam = Camera.main;
            if (cam != null)
            {
                cam.transform.position = new Vector3(0, 4.5f, -19f);
                var follow = cam.gameObject.AddComponent<ThirdPersonCamera>();
                var so = new SerializedObject(follow);
                so.FindProperty("target").objectReferenceValue = player.transform;
                so.ApplyModifiedPropertiesWithoutUndo();
            }

            var light = Object.FindObjectOfType<Light>();
            if (light != null)
            {
                light.transform.rotation = Quaternion.Euler(48f, -32f, 0f);
                light.intensity = 1.15f;
            }

            EditorSceneManager.SaveScene(scene, "Assets/Scenes/FreeCity_Prototype.unity");
            AssetDatabase.SaveAssets();
            Debug.Log("FreeCity prototype scene generated. Press Play to test movement.");
        }

        private static Material MakeMaterial(string name, Color color)
        {
            Shader shader = Shader.Find("Standard");
            var material = new Material(shader) { name = name, color = color };
            return material;
        }

        private static void CreateCube(string name, Vector3 position, Vector3 scale, Material material)
        {
            var cube = GameObject.CreatePrimitive(PrimitiveType.Cube);
            cube.name = name;
            cube.transform.position = position;
            cube.transform.localScale = scale;
            cube.GetComponent<Renderer>().sharedMaterial = material;
        }
    }
}
