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
            CreateDistrictRoads(road);

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
            new GameObject("FreeCityTouchInput").AddComponent<FreeCityTouchInput>();

            CreateStarterCar();
            new GameObject("AmbientTraffic").AddComponent<FreeCityTraffic>();
            new GameObject("PedestrianAmbience").AddComponent<FreeCityPedestrians>();
            CreateMissionCheckpoint();

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

            if (!AssetDatabase.IsValidFolder("Assets/Scenes"))
                AssetDatabase.CreateFolder("Assets", "Scenes");
            EditorSceneManager.SaveScene(scene, "Assets/Scenes/FreeCity_Prototype.unity");
            AssetDatabase.SaveAssets();
            Debug.Log("FreeCity prototype scene generated. Press Play to test movement.");
        }

        private static void CreateDistrictRoads(Material road)
        {
            Material poleMat = MakeMaterial("StreetLightMetal", new Color(0.12f, 0.14f, 0.16f));
            Material lampMat = MakeMaterial("StreetLightLamp", new Color(1f, 0.82f, 0.48f));
            for (int i = -3; i <= 3; i++)
            {
                if (i == 0) continue;
                float p = i * 42f + (i > 0 ? -21f : 21f);
                CreateCube("GridRoad_NS_" + i, new Vector3(p, -0.035f, 0), new Vector3(7f, 0.08f, 500f), road);
                CreateCube("GridRoad_EW_" + i, new Vector3(0, -0.03f, p), new Vector3(500f, 0.08f, 7f), road);
                for (int j = -4; j <= 4; j++)
                {
                    float z = j * 48f;
                    CreateStreetLight(new Vector3(p + 5.5f, 0f, z), poleMat, lampMat);
                    CreateStreetLight(new Vector3(z, 0f, p + 5.5f), poleMat, lampMat);
                }
            }
        }

        private static void CreateStreetLight(Vector3 position, Material poleMat, Material lampMat)
        {
            CreateCube("StreetLightPole", position + Vector3.up * 3f, new Vector3(0.18f, 6f, 0.18f), poleMat);
            CreateCube("StreetLightArm", position + new Vector3(0.65f, 5.8f, 0f), new Vector3(1.3f, 0.14f, 0.14f), poleMat);
            CreateCube("StreetLightHead", position + new Vector3(1.2f, 5.65f, 0f), new Vector3(0.55f, 0.18f, 0.4f), lampMat);
        }

        private static void CreateMissionCheckpoint()
        {
            var checkpoint = GameObject.CreatePrimitive(PrimitiveType.Cylinder);
            checkpoint.name = "MissionCheckpoint";
            checkpoint.transform.position = new Vector3(0f, 0.12f, 105f);
            checkpoint.transform.localScale = new Vector3(4f, 0.25f, 4f);
            checkpoint.GetComponent<Renderer>().sharedMaterial =
                MakeMaterial("CheckpointGlow", new Color(0.1f, 0.85f, 1f));
            checkpoint.GetComponent<CapsuleCollider>().isTrigger = true;
            var checkpointBody = checkpoint.AddComponent<Rigidbody>();
            checkpointBody.isKinematic = true;
            checkpointBody.useGravity = false;
            var mission = checkpoint.AddComponent<FreeCityMission>();
            var serialized = new SerializedObject(mission);
            serialized.FindProperty("destination").objectReferenceValue = checkpoint.transform;
            serialized.ApplyModifiedPropertiesWithoutUndo();
        }

        private static void CreateStarterCar()
        {
            var car = new GameObject("StarterCar");
            car.transform.position = new Vector3(3f, 0.8f, -12f);
            var carBody = car.AddComponent<Rigidbody>();
            carBody.isKinematic = true;
            carBody.useGravity = false;
            carBody.interpolation = RigidbodyInterpolation.Interpolate;
            car.AddComponent<FreeCityVehicle>();

            var body = GameObject.CreatePrimitive(PrimitiveType.Cube);
            body.name = "CarBody";
            body.transform.SetParent(car.transform, false);
            body.transform.localPosition = new Vector3(0f, 0.35f, 0f);
            body.transform.localScale = new Vector3(2f, 0.7f, 4f);
            body.GetComponent<Renderer>().sharedMaterial =
                MakeMaterial("VehiclePaint", new Color(0.08f, 0.22f, 0.48f));

            var cabin = GameObject.CreatePrimitive(PrimitiveType.Cube);
            cabin.name = "Cabin";
            cabin.transform.SetParent(car.transform, false);
            cabin.transform.localPosition = new Vector3(0f, 0.95f, -0.25f);
            cabin.transform.localScale = new Vector3(1.65f, 0.65f, 1.8f);
            cabin.GetComponent<Renderer>().sharedMaterial =
                MakeMaterial("GlassTint", new Color(0.12f, 0.18f, 0.22f));

            var trigger = car.AddComponent<SphereCollider>();
            trigger.isTrigger = true;
            trigger.radius = 3.5f;
            var seat = new GameObject("SeatPoint");
            seat.transform.SetParent(car.transform, false);
            seat.transform.localPosition = new Vector3(0f, 0.8f, 0f);
            var vehicle = car.GetComponent<FreeCityVehicle>();
            var serialized = new SerializedObject(vehicle);
            serialized.FindProperty("seatPoint").objectReferenceValue = seat.transform;
            serialized.ApplyModifiedPropertiesWithoutUndo();

            for (int side = -1; side <= 1; side += 2)
            for (int axle = -1; axle <= 1; axle += 2)
            {
                var wheel = GameObject.CreatePrimitive(PrimitiveType.Cylinder);
                wheel.name = "Wheel";
                wheel.transform.SetParent(car.transform, false);
                wheel.transform.localPosition = new Vector3(side * 1.05f, 0f, axle * 1.25f);
                wheel.transform.localRotation = Quaternion.Euler(0f, 0f, 90f);
                wheel.transform.localScale = new Vector3(0.65f, 0.22f, 0.65f);
                wheel.GetComponent<Renderer>().sharedMaterial =
                    MakeMaterial("Rubber", new Color(0.025f, 0.025f, 0.03f));
            }
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
