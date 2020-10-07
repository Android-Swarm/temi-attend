package com.zetzaus.temiattend.di

import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.database.Attendance
import com.zetzaus.temiattend.database.AttendanceDatabase
import com.zetzaus.temiattend.face.AzureFaceManager
import com.zetzaus.temiattend.face.MaskDetector
import com.zetzaus.temiattend.ui.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@InstallIn(ApplicationComponent::class)
@Module
class Modules {

    @Provides
    fun provideAttendanceDao(@ApplicationContext context: Context) =
        AttendanceDatabase.getInstance(context).dao

    @Provides
    fun provideAzureFaceManager(@ApplicationContext context: Context) =
        AzureFaceManager(
            endpoint = context.getString(R.string.azure_endpoint),
            apiKey = context.getString(R.string.azure_key)
        )
}

@InstallIn(FragmentComponent::class)
@Module
class FragmentModules {
    @Provides
    fun provideAttendanceDiffUtilCallback() = object : DiffUtil.ItemCallback<Attendance>() {
        override fun areItemsTheSame(oldItem: Attendance, newItem: Attendance) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Attendance, newItem: Attendance) =
            oldItem == newItem
    }
}

@InstallIn(ActivityComponent::class)
@Module
class MainActivityModules {

    @Provides
    fun provideFaceDetector() = FaceDetectorOptions.Builder()
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .build().run {
            FaceDetection.getClient(this)
        }

    @Provides
    fun provideMaskDetector(): MaskDetector {
        // Load custom TFlite Model
        val localModel = LocalModel.Builder()
            .setAssetFilePath(MainActivity.MODEL_FILENAME)
            .build()

        // Image classification configurations
        val customLabelerOptions = CustomImageLabelerOptions.Builder(localModel)
            .setConfidenceThreshold(0.8f)
            .build()

        return MaskDetector(customLabelerOptions)
    }
}