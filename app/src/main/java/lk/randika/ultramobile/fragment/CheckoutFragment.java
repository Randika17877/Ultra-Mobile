package lk.randika.ultramobile.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lk.randika.ultramobile.R;
import lk.randika.ultramobile.databinding.FragmentCheckoutBinding;
import lk.randika.ultramobile.listener.FirestoreCallback;
import lk.randika.ultramobile.model.CartItem;
import lk.randika.ultramobile.model.Order;
import lk.randika.ultramobile.model.Product;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;


public class CheckoutFragment extends Fragment {
    private FragmentCheckoutBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private double total;
    private boolean paymentActive;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);


        binding.shippingLayoutBtn.setOnClickListener(v -> {

            if (binding.shippingLayoutBody.getVisibility() == View.GONE) {
                binding.shippingLayoutBody.setVisibility(View.VISIBLE);
                binding.shippingLayoutBtn.setRotation(180f);
            } else {
                binding.shippingLayoutBody.setVisibility(View.GONE);
                binding.shippingLayoutBtn.setRotation(0f);
            }
        });

        binding.billingLayoutBtn.setOnClickListener(v -> {
            if (binding.billingLayoutBody.getVisibility() == View.GONE) {
                binding.billingLayoutBody.setVisibility(View.VISIBLE);
                binding.billingLayoutBtn.setRotation(180f);
            } else {
                binding.billingLayoutBody.setVisibility(View.GONE);
                binding.billingLayoutBtn.setRotation(0f);
            }
        });


        binding.shippingDetailsCheckBilling.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.billingLayout.setVisibility(View.GONE);
            } else {
                binding.billingLayout.setVisibility(View.VISIBLE);
                binding.billingLayoutBody.setVisibility(View.VISIBLE);
                binding.billingLayoutBtn.setRotation(180f);
            }
        });


        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        double shippingCost = 400;

        getCartItems(cartItems -> {

            ArrayList<String> productIds = new ArrayList<>();
            cartItems.forEach(cartItem -> {
                productIds.add(cartItem.getProductId());
            });


            getProductsByIds(productIds, data -> {

                double subTotal = 0;

                for (CartItem cartItem : cartItems) {
                    Product product = data.get(cartItem.getProductId());
                    if (product != null) {
                        subTotal += product.getPrice() * cartItem.getQuantity();
                    }
                }

                total = subTotal + shippingCost;
                binding.checkoutSubtotal.setText(String.format(Locale.US, "LKR %,.2f", subTotal));
                binding.checkoutShipping.setText(String.format(Locale.US, "LKR %,.2f", shippingCost));
                binding.checkoutTotal.setText(String.format(Locale.US, "LKR %,.2f", total));
                paymentActive = true;
            });

        });


        binding.checkoutBtnProceed.setOnClickListener(v -> {

            if (paymentActive && validateFields()) {
                InitRequest req = new InitRequest();
                req.setSandBox(true);

                req.setMerchantId("1226855");
                req.setMerchantSecret("MTkwMzkxMTc1MjQxMTE0MDU4MDc0NzcyODk2NzM2NTAwMDUwOTM=");
                req.setCurrency("LKR");
                req.setAmount(total);
                req.setOrderId("ORD-" + System.currentTimeMillis());

                req.setItemsDescription("UltraMobile Store Order");

                req.getCustomer().setFirstName(binding.shippingDetailsName.getText().toString());
                req.getCustomer().setLastName("");
                req.getCustomer().setEmail(binding.shippingDetailsEmail.getText().toString());
                req.getCustomer().setPhone(binding.shippingDetailsContact.getText().toString());
                req.getCustomer().getAddress().setAddress(binding.shippingDetailsAddress1.getText().toString());
                req.getCustomer().getAddress().setCity(binding.shippingDetailsCity.getText().toString());
                req.getCustomer().getAddress().setCountry("Sri Lanka");

                req.setNotifyUrl("https://example.com/notify");

                Intent intent = new Intent(getActivity(), PHMainActivity.class);
                intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);

                payhereLauncher.launch(intent);
            }

        });


    }

    private boolean validateFields() {
        boolean isValid = true;

        // Shipping validation
        if (!validateEditText(binding.shippingDetailsName, "Name is required")) isValid = false;
        if (!validateEmail(binding.shippingDetailsEmail)) isValid = false;
        if (!validateEditText(binding.shippingDetailsContact, "Contact is required")) isValid = false;
        if (!validateEditText(binding.shippingDetailsAddress1, "Address is required")) isValid = false;
        if (!validateEditText(binding.shippingDetailsCity, "City is required")) isValid = false;
        if (!validateEditText(binding.shippingDetailsPostcode, "Postcode is required")) isValid = false;

        if (!binding.shippingDetailsCheckBilling.isChecked()) {
            // Billing validation
            if (!validateEditText(binding.billingDetailsName, "Name is required")) isValid = false;
            if (!validateEmail(binding.billingDetailsEmail)) isValid = false;
            if (!validateEditText(binding.billingDetailsContact, "Contact is required")) isValid = false;
            if (!validateEditText(binding.billingDetailsAddress1, "Address is required")) isValid = false;
            if (!validateEditText(binding.billingDetailsCity, "City is required")) isValid = false;
            if (!validateEditText(binding.billingDetailsPostcode, "Postcode is required")) isValid = false;
        }

        if (!isValid) {
            Toast.makeText(getContext(), "Please correct the errors in the form", Toast.LENGTH_SHORT).show();
            // Scroll to top or ensure visible if needed, but error messages on fields are usually enough
        }

        return isValid;
    }

    private boolean validateEditText(EditText editText, String errorMessage) {
        String value = editText.getText().toString().trim();
        if (value.isEmpty()) {
            editText.setError(errorMessage);
            return false;
        }
        return true;
    }

    private boolean validateEmail(EditText editText) {
        String email = editText.getText().toString().trim();
        if (email.isEmpty()) {
            editText.setError("Email is required");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editText.setError("Invalid email address");
            return false;
        }
        return true;
    }


    private void getCartItems(FirestoreCallback<List<CartItem>> callback) {
        if (getArguments() != null && getArguments().containsKey("buyNowItem")) {
            CartItem buyNowItem = (CartItem) getArguments().getSerializable("buyNowItem");
            if (buyNowItem != null) {
                List<CartItem> items = new ArrayList<>();
                items.add(buyNowItem);
                callback.onCallback(items);
                return;
            }
        }

        String uid = firebaseAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).collection("cart").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot qds) {
                List<CartItem> cartItems = new ArrayList<>();
                if (qds != null && !qds.isEmpty()) {
                    cartItems = qds.toObjects(CartItem.class);
                }
                callback.onCallback(cartItems);
            }
        });
    }

    private void getProductsByIds(List<String> productIds, FirestoreCallback<Map<String, Product>> callback) {

        Map<String, Product> products = new HashMap<>();

        if (productIds == null || productIds.isEmpty()) {
            callback.onCallback(products);
            return;
        }

        db.collection("products").whereIn("productId", productIds).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot qds) {

                qds.getDocuments().forEach(ds -> {
                    Product product = ds.toObject(Product.class);
                    if (product != null) {
                        products.put(product.getProductId(), product);
                    }
                });

                callback.onCallback(products);
            }
        });

    }

    private final ActivityResultLauncher<Intent> payhereLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Intent data = result.getData();
            if (data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
                PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);

                if (response != null && response.isSuccess()) {

                    StatusResponse statusResponse = response.getData();


                    // Save order to firestore
                    saveOrder(statusResponse);


                    Log.i("PAYHERE", "Payment Success!");

                } else {
                    Log.e("PAYHERE", response.getData() != null ? response.getData().getMessage() : "Payment Failed");

                }

            }
        } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
            Log.e("PAYHERE", "Payment Canceled!");
            Log.e("PAYHERE", String.valueOf(total));
        }

    });

    private void saveOrder(StatusResponse statusResponse) {
        getCartItems(cartItems -> {

            String uid = firebaseAuth.getCurrentUser().getUid();

            Order order = new Order();
            order.setOrderId(String.valueOf(System.currentTimeMillis()));
            order.setUserId(uid);
            order.setTotalAmount(total);
            order.setStatus("PAID");
            order.setOrderDate(Timestamp.now());


            String shipping_name = binding.shippingDetailsName.getText().toString();
            String shipping_email = binding.shippingDetailsEmail.getText().toString();
            String shipping_contact = binding.shippingDetailsContact.getText().toString();
            String shipping_address1 = binding.shippingDetailsAddress1.getText().toString();
            String shipping_address2 = binding.shippingDetailsAddress2.getText().toString();
            String shipping_city = binding.shippingDetailsCity.getText().toString();
            String shipping_postCode = binding.shippingDetailsPostcode.getText().toString();

            Order.Address shippingAddress = Order.Address.builder().name(shipping_name).email(shipping_email).contact(shipping_contact).address1(shipping_address1).address2(shipping_address2).city(shipping_city).postcode(shipping_postCode).build();

            order.setShippingAddress(shippingAddress);

            if (!binding.shippingDetailsCheckBilling.isChecked()) {
                String billing_name = binding.billingDetailsName.getText().toString();
                String billing_email = binding.billingDetailsEmail.getText().toString();
                String billing_contact = binding.billingDetailsContact.getText().toString();
                String billing_address1 = binding.billingDetailsAddress1.getText().toString();
                String billing_address2 = binding.billingDetailsAddress2.getText().toString();
                String billing_city = binding.billingDetailsCity.getText().toString();
                String billing_postCode = binding.billingDetailsPostcode.getText().toString();

                Order.Address billingAddress = Order.Address.builder().name(billing_name).email(billing_email).contact(billing_contact).address1(billing_address1).address2(billing_address2).city(billing_city).postcode(billing_postCode).build();

                order.setBillingAddress(billingAddress);
            }

            /// //////
            ArrayList<String> productIds = new ArrayList<>();
            cartItems.forEach(cartItem -> {
                productIds.add(cartItem.getProductId());
            });

            List<Order.OrderItem> orderItems = new ArrayList<>();

            getProductsByIds(productIds, data -> {

                for (CartItem cartItem : cartItems) {
                    Product product = data.get(cartItem.getProductId());

                    if (product != null) {

                        List<Order.OrderItem.Attribute> attributes = new ArrayList<>();

                        for (CartItem.Attribute at : cartItem.getAttributes()) {
                            Order.OrderItem.Attribute attribute = Order.OrderItem.Attribute.builder().name(at.getName()).value(at.getValue()).build();

                            attributes.add(attribute);
                        }

                        String firstImage = (product.getImages() != null && !product.getImages().isEmpty()) ? product.getImages().get(0) : null;
                        Order.OrderItem orderItem = Order.OrderItem.builder()
                                .productId(cartItem.getProductId())
                                .productTitle(product.getTitle())
                                .imageUrl(firstImage)
                                .unitPrice(product.getPrice())
                                .quantity(cartItem.getQuantity())
                                .attributes(attributes)
                                .build();
                        orderItems.add(orderItem);

                        ///  Add order items to Order object
                        order.setOrderItems(orderItems);

                    }
                }
                db.collection("orders").document().set(order).addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Order Saved!", Toast.LENGTH_SHORT).show();

                    // Clear cart only if this was a cart checkout (not a Buy Now checkout)
                    if (getArguments() == null || !getArguments().containsKey("buyNowItem")) {
                        db.collection("users").document(uid).collection("cart")
                                .get()
                                .addOnSuccessListener(qds -> {
                                    qds.getDocuments().forEach(ds -> {
                                        ds.getReference().delete();
                                    });
                                });
                    }

                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new HomeFragment())
                            .commit();

                });

            });


        });
    }

}