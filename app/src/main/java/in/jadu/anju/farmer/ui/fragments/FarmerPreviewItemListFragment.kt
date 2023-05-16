package `in`.jadu.anju.farmer.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import `in`.jadu.anju.R
import `in`.jadu.anju.databinding.FragmentFarmerPreviewItemListBinding
import `in`.jadu.anju.farmer.viewmodels.ContractOperationViewModel
import `in`.jadu.anju.farmer.viewmodels.FarmerListItemViewModel
import `in`.jadu.anju.farmer.viewmodels.WalletConnectViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.FileNotFoundException

@AndroidEntryPoint
class FarmerPreviewItemListFragment : Fragment() {
    private val farmerListItemViewModel: FarmerListItemViewModel by viewModels()
    private val contractOperationViewModel: ContractOperationViewModel by viewModels()
    private val walletConnectViewModel: WalletConnectViewModel by viewModels()
    private lateinit var binding: FragmentFarmerPreviewItemListBinding
    private var productName: String? = null
    private var productDescription: String? = null
    private var seedingDate: String? = null
    private var expiryDate: String? = null
    private var productPrice: String? = null
    private var productType: String? = null
    private var getUri: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFarmerPreviewItemListBinding.inflate(inflater, container, false)
        setPreviewData()
        binding.btnPreviewBtn.setOnClickListener {
            binding.lottieProgress.visibility = View.VISIBLE
            binding.btnPreviewBtn.visibility = View.GONE
            uploadDataToServer()
        }
        binding.btnEditFields.setOnClickListener {
            findNavController().navigate(R.id.action_farmerPreviewItemListFragment2_to_farmerListItemFragment2)
        }
        lifecycleScope.launch {
            farmerListItemViewModel.mainEvent.collect() { event ->
                when (event) {
                    is FarmerListItemViewModel.MainEvent.Error -> {
                        Toast.makeText(requireContext(), event.error, Toast.LENGTH_SHORT).show()
                        binding.lottieProgress.visibility = View.GONE
                        binding.btnPreviewBtn.visibility = View.VISIBLE
                    }
                    is FarmerListItemViewModel.MainEvent.Success -> {
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                        if(event.message == "Product Created Successfully"){
                            findNavController().navigate(R.id.action_farmerPreviewItemListFragment2_to_successListingFragment)
                        }
                    }
                }
            }
        }
        setupContractOperation(walletConnectViewModel.getPrivateKey())
        return binding.root
    }
    private fun setupContractOperation(privateKey: String) {
        contractOperationViewModel.deployContract(privateKey,requireContext())
    }
    private fun uploadDataToServer() {
        //upload Data to server
        val imagePart = farmerListItemViewModel.getImagePart(getUriFromPath(getUri!!), requireContext())
        Log.d("imagepart", "uploadDataToServer: ${imagePart.body}")
        farmerListItemViewModel.createProductRemote(
            productType!!,
            productName!!,
            imagePart,
            productDescription!!,
            seedingDate!!,
            expiryDate!!,
            productPrice!!
        )
    }

    private fun getUriFromPath(path: String): Uri {
        return Uri.parse(path)
    }

    private fun setPreviewData() {
        productName = arguments?.getString("productName")
        productDescription = arguments?.getString("productDescription")
        seedingDate = arguments?.getString("harvestedDate")
        expiryDate = arguments?.getString("expiryDate")
        productPrice = arguments?.getString("productPrice")
        productType = arguments?.getString("productType")
        getUri = arguments?.getString("ImageUri")
        val farmLocation = arguments?.getString("farmLocation")
        binding.apply {
            tvProductType.text = productType
            tvProductName.setText(productName)
            tvProductDescription.setText(productDescription)
            tvHarvestedDate.setText(seedingDate)
            tvExpiryDate.setText(expiryDate)
            tvProductPrice.setText(productPrice)
            ivCustomimageselect.setImageURI(getUriFromPath(getUri!!))
            ivVegetables.setImageURI(getUriFromPath(getUri!!))
            tvFarmLocation.setText(farmLocation)

            tvProductName.keyListener = null
            tvProductDescription.keyListener = null
            tvHarvestedDate.keyListener = null
            tvExpiryDate.keyListener = null
            tvProductPrice.keyListener = null
            tvFarmLocation.keyListener = null

        }
    }

}