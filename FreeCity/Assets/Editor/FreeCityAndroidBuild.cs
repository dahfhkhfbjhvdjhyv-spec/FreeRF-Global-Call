using UnityEditor;
using UnityEditor.Build.Reporting;
using UnityEngine;

namespace FreeCity.Editor
{
    public static class FreeCityAndroidBuild
    {
        public static void Build()
        {
            FreeCityBootstrap.Generate();
            PlayerSettings.companyName = "FreeCity Studio";
            PlayerSettings.productName = "FreeCity";
            PlayerSettings.defaultInterfaceOrientation = UIOrientation.LandscapeLeft;
            PlayerSettings.stripEngineCode = true;
            PlayerSettings.SetManagedStrippingLevel(BuildTargetGroup.Android, ManagedStrippingLevel.Medium);
            PlayerSettings.applicationIdentifier = "com.freecity.openworld";
            PlayerSettings.Android.minSdkVersion = AndroidSdkVersions.AndroidApiLevel26;
            PlayerSettings.Android.targetArchitectures = AndroidArchitecture.ARM64;
            PlayerSettings.SetScriptingBackend(BuildTargetGroup.Android, ScriptingImplementation.IL2CPP);
            EditorUserBuildSettings.buildAppBundle = false;
            System.IO.Directory.CreateDirectory("Build");

            var options = new BuildPlayerOptions
            {
                scenes = new[] { "Assets/Scenes/FreeCity_Prototype.unity" },
                locationPathName = "Build/FreeCity.apk",
                target = BuildTarget.Android,
                options = BuildOptions.None
            };
            BuildReport report = BuildPipeline.BuildPlayer(options);
            if (report.summary.result != BuildResult.Succeeded)
                throw new System.Exception("FreeCity Android build failed: " + report.summary.result);
            Debug.Log("FreeCity APK built: " + report.summary.totalSize + " bytes");
        }
    }
}
