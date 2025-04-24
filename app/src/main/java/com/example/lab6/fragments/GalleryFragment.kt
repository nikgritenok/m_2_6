package com.example.lab6.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lab6.R
import com.example.lab6.adapter.ImageAdapter
import com.example.lab6.databinding.DialogDescriptionBinding
import com.example.lab6.databinding.FragmentGalleryBinding
import com.example.lab6.model.ImageItem

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageAdapter: ImageAdapter
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            openImagePicker()
        }
    }
    
    private val pickImagesLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent ->

                if (intent.data != null) {
                    val uri = intent.data!!
                    addImageToGallery(uri)
                }

                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    if (intent.clipData != null) {
                        val clipData = intent.clipData!!
                        for (i in 0 until clipData.itemCount) {
                            val uri = clipData.getItemAt(i).uri
                            addImageToGallery(uri)
                        }
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        
        binding.fabLoadImages.setOnClickListener {
            checkPermissionsAndOpenGallery()
        }
    }
    
    private fun setupRecyclerView() {
        imageAdapter = ImageAdapter(
            onItemClick = { imageItem ->
                navigateToDetail(imageItem)
            },
            onItemLongClick = { imageItem, position ->
                showDescriptionDialog(imageItem, position)
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = imageAdapter
        }
    }
    
    private fun navigateToDetail(imageItem: ImageItem) {
        val action = GalleryFragmentDirections.actionGalleryFragmentToDetailFragment(
            imageUri = imageItem.uri.toString(),
            imageDescription = imageItem.description
        )
        findNavController().navigate(action)
    }
    
    private fun checkPermissionsAndOpenGallery() {
        val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        
        val allPermissionsGranted = permissionsToRequest.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
        
        if (allPermissionsGranted) {
            openImagePicker()
        } else {
            requestPermissionLauncher.launch(permissionsToRequest)
        }
    }
    
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        pickImagesLauncher.launch(intent)
    }
    
    private fun addImageToGallery(uri: Uri) {
        // Сохраняем доступ к URI для последующего использования
        requireContext().contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        
        val imageItem = ImageItem(uri)
        imageAdapter.addImage(imageItem)
    }
    
    private fun showDescriptionDialog(imageItem: ImageItem, position: Int) {
        val dialogBinding = DialogDescriptionBinding.inflate(layoutInflater)
        dialogBinding.editTextDescription.setText(imageItem.description)
        
        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Сохранить") { _, _ ->
                val newDescription = dialogBinding.editTextDescription.text.toString()
                imageAdapter.updateImageDescription(position, newDescription)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 