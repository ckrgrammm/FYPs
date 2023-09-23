package com.example.kleine.fragments.admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.kleine.R
import com.example.kleine.databinding.FragmentAdminAddRewardBinding
import com.example.kleine.databinding.FragmentAdminUpdateRewardBinding
import com.example.kleine.viewmodel.admin.AdminAddRewardViewModel
import com.example.kleine.viewmodel.admin.AdminUpdateRewardViewModel

class AdminUpdateRewardFragment : Fragment() {

    companion object {
        private const val REQUEST_CODE_SELECT_IMAGE = 1234
    }

    private lateinit var binding: FragmentAdminUpdateRewardBinding
    private lateinit var viewModel: AdminUpdateRewardViewModel
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_admin_update_reward, container, false)
        viewModel = ViewModelProvider(this).get(AdminUpdateRewardViewModel::class.java)
        binding.adminUpdateRewardViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val documentId = arguments?.getString("documentId")
        viewModel.loadRewardDetails(documentId)

        viewModel.imageUrl.observe(viewLifecycleOwner, Observer { url ->
            url?.let {
                val imageView: ImageView = binding.imgRewardPreview
                Glide.with(this@AdminUpdateRewardFragment)
                    .load(it)
                    .into(imageView)
            }
        })

        binding.imgRewardPreview.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
        }

        binding.btnUpdate.setOnClickListener {
            val rewardName = binding.txtRewardName.text.toString().trim()
            val rewardDescription = binding.txtRewardDescription.text.toString().trim()
            val rewardPoints = binding.txtRewardPoints.text.toString().toIntOrNull()
            val redeemLimit = binding.txtRedeemLimit.text.toString().toIntOrNull()

            when {
                selectedImageUri == null && viewModel.imageUrl.value.isNullOrEmpty() -> {
                    Toast.makeText(context, "Please select an image!", Toast.LENGTH_SHORT).show()
                    // Preventing submission by returning early
                    return@setOnClickListener
                }
                rewardName.isEmpty() -> {
                    Toast.makeText(context, "Reward name cannot be empty!", Toast.LENGTH_SHORT).show()
                    binding.txtRewardName.requestFocus()
                    // Preventing submission by returning early
                    return@setOnClickListener
                }
                rewardDescription.isEmpty() -> {
                    Toast.makeText(context, "Reward description cannot be empty!", Toast.LENGTH_SHORT).show()
                    binding.txtRewardDescription.requestFocus()
                    // Preventing submission by returning early
                    return@setOnClickListener
                }
                rewardPoints == null || rewardPoints <= 0 -> {
                    Toast.makeText(context, "Reward points must be greater than zero!", Toast.LENGTH_SHORT).show()
                    binding.txtRewardPoints.requestFocus()
                    // Preventing submission by returning early
                    return@setOnClickListener
                }
                redeemLimit == null || redeemLimit <= 0 -> {
                    Toast.makeText(context, "Redeem limit must be greater than zero!", Toast.LENGTH_SHORT).show()
                    binding.txtRedeemLimit.requestFocus()
                    // Preventing submission by returning early
                    return@setOnClickListener
                }
                else -> {
                    // All validations passed, proceeding with the submission
                    viewModel.updateRewardDetailsWithImage(documentId, selectedImageUri)
                }
            }

        }

        viewModel.updateResult.observe(viewLifecycleOwner, Observer { result ->
            if (result) {
                Toast.makeText(context, "Reward updated successfully!", Toast.LENGTH_SHORT).show()
                // Navigate back to AdminViewRewardFragment
                findNavController().navigateUp()
            }
        })

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            selectedImageUri?.let {
                binding.imgRewardPreview.setImageURI(it)
            }
        }
    }

}
