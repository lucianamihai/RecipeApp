package com.example.flavormix.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flavormix.adapters.IngredientAdapter
import com.example.flavormix.databinding.FragmentShoppingCartBinding
import com.example.flavormix.viewModel.ShoppingCartViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class ShoppingCartFragment : Fragment() {

    private lateinit var viewModel: ShoppingCartViewModel
    private lateinit var adapter: IngredientAdapter
    private var _binding: FragmentShoppingCartBinding? = null
    private val binding get() = _binding!!

    // launcher pentru selectarea imaginii din galerie
    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            processImage(it)
        }
    }

    // launcher pentru capturarea imaginii folosind camera
    private val captureImageLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            // Imaginea a fost capturata si salvata
            val imageUri: Uri? = viewModel.imageUri
            imageUri?.let {
                processImage(it)
            }
        } else {
            Toast.makeText(context, "Eroare la capturarea imaginii", Toast.LENGTH_SHORT).show()
        }
    }

    // cod de cerere pentru permisiuni camera
    private val CAMERA_PERMISSION_REQUEST_CODE = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentShoppingCartBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ShoppingCartViewModel::class.java]
        adapter = IngredientAdapter { ingredient ->
            viewModel.deleteIngredient(ingredient)
        }
        binding.ingredientRecyclerView.layoutManager = LinearLayoutManager(context)

        binding.ingredientRecyclerView.adapter = adapter
        binding.ingredientRecyclerView.visibility = View.VISIBLE

        binding.addButton.setOnClickListener {
            val ingredientName = binding.ingredientEditText.text.toString()
            if (ingredientName.isNotBlank()) {
                viewModel.addIngredient(ingredientName)
                binding.ingredientEditText.text.clear()
            } else {
                Toast.makeText(context, "Please enter an ingredient", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.ingredients.observe(viewLifecycleOwner) { ingredients ->
            if (ingredients.isNotEmpty()) {
                Log.d("ShoppingCartFragment", "Ingredients loaded: ${ingredients.size}")
            } else {
                Log.d("ShoppingCartFragment", "No ingredients found")
            }
            adapter.submitList(ingredients)
            adapter.notifyDataSetChanged()
        }
        binding.btnAddIngredientViaPhoto.setOnClickListener {
            showImageSelectionOptions()
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = IngredientAdapter { ingredient ->
            viewModel.deleteIngredient(ingredient)
        }
        binding.ingredientRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = adapter
            visibility = View.VISIBLE
        }
    }

    private fun observeIngredients() {
        viewModel.ingredients.observe(viewLifecycleOwner) { ingredients ->
            if (ingredients.isNotEmpty()) {
                Log.d("ShoppingCartFragment", "Ingredients loaded: ${ingredients.size}")
            } else {
                Log.d("ShoppingCartFragment", "No ingredients found")
            }
            adapter.submitList(ingredients)
        }
    }

    private fun showImageSelectionOptions() {
        // dialog pentru a permite utilizatorului sa aleaga intre galerie si camera
        val options = arrayOf("Select from gallery", "Use camera")
        AlertDialog.Builder(requireContext())
            .setTitle("Add igredients through photo")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> selectImageFromGallery()
                    1 -> {
                        if (hasCameraPermission()) {
                            captureImageWithCamera()
                        } else {
                            requestCameraPermission()
                        }
                    }
                }
            }
            .show()
    }

    private fun selectImageFromGallery() {
        // deschide galeria pentru a selecta o imagine
        selectImageLauncher.launch("image/*")
    }

    private fun captureImageWithCamera() {
        // creeaza un URI pentru a salva imaginea capturata
        val tempImageUri = viewModel.createImageUri()
        if (tempImageUri != null) {
            viewModel.setImageUri(tempImageUri)
            captureImageLauncher.launch(tempImageUri)
        } else {
            Toast.makeText(context, "Eroare la crearea fișierului temporar", Toast.LENGTH_SHORT).show()
        }
    }

    // verificarea permisiunilor pentru camera
    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    // solicitarea permisiunii pentru camera
    private fun requestCameraPermission() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    captureImageWithCamera()
                } else {
                    Toast.makeText(context, "Permisiunea pentru camera este necesara pentru a captura imagini.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun processImage(imageUri: Uri) {
        try {
            //conversie in InputImage (format pt ML KIT)
            val image = InputImage.fromFilePath(requireContext(), imageUri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val extractedText = visionText.text
                    Log.d("ShoppingCartFragment", "Text extras: $extractedText")//procesarea a reusit textul e preluat
                    parseAndAddIngredients(extractedText)//parseAndAddIngredients extrage fiecare linie ca ingredient
                }
                .addOnFailureListener { e ->
                    Log.e("ShoppingCartFragment", "Eroare OCR: ${e.message}")
                    Toast.makeText(context, "Eroare la procesarea imaginii", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Log.e("ShoppingCartFragment", "Eroare la procesarea imaginii: ${e.message}")
            Toast.makeText(context, "Eroare la procesarea imaginii", Toast.LENGTH_SHORT).show()
        }
    }

    private fun parseAndAddIngredients(text: String) {
        // fiecare linie reprezintă un ingredient
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }

        if (lines.isEmpty()) {
            Toast.makeText(context, "Nu s-au extras ingrediente din imagine", Toast.LENGTH_SHORT).show()
            return
        }

        // adauga fiecare ingredient in baza de date
        lines.forEach { ingredientName ->
            viewModel.addIngredient(ingredientName)
        }

        Toast.makeText(context, "Ingrediente adaugate: ${lines.size}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
