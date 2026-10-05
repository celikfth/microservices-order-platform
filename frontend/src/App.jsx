import { useEffect, useState } from "react";
import "./App.css";


// =====================================================
// API CONFIGURATION
// =====================================================

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL;


function App() {

  // =====================================================
  // STATE
  // =====================================================

  const [products, setProducts] = useState([]);

  const [cart, setCart] = useState([]);

  const [showCheckout, setShowCheckout] =
    useState(false);

  const [guestName, setGuestName] =
    useState("");

  const [guestEmail, setGuestEmail] =
    useState("");

  const [guestPhone, setGuestPhone] =
    useState("");

  const [paymentMethod, setPaymentMethod] =
    useState("STRIPE");

  const [isSubmitting, setIsSubmitting] =
    useState(false);

  const [orderResult, setOrderResult] =
    useState(null);

  const [errorMessage, setErrorMessage] =
    useState("");

  const [productError, setProductError] =
    useState("");


  // =====================================================
  // LOAD PRODUCTS
  // =====================================================

  useEffect(() => {

    const loadProducts = async () => {

      try {

        setProductError("");


        const response =
          await fetch(
            `${API_BASE_URL}/api/products`
          );


        if (!response.ok) {

          throw new Error(
            "Could not load products."
          );

        }


        const data =
          await response.json();


        setProducts(data);


      } catch (error) {

        console.error(
          "Error loading products:",
          error
        );


        setProductError(
          "Products could not be loaded. Please make sure the API Gateway and Product Service are running."
        );

      }

    };


    loadProducts();

  }, []);


  // =====================================================
  // ADD TO CART
  // =====================================================

  const addToCart = (product) => {

    const existingItem =
      cart.find(
        (item) =>
          item.id === product.id
      );


    if (existingItem) {

      setCart(

        cart.map((item) =>

          item.id === product.id

            ? {
                ...item,
                quantity:
                  item.quantity + 1
              }

            : item

        )

      );

    } else {

      setCart([

        ...cart,

        {
          ...product,
          quantity: 1
        }

      ]);

    }

  };


  // =====================================================
  // DECREASE QUANTITY
  // =====================================================

  const decreaseQuantity = (productId) => {

    setCart(

      cart

        .map((item) =>

          item.id === productId

            ? {
                ...item,
                quantity:
                  item.quantity - 1
              }

            : item

        )

        .filter(
          (item) =>
            item.quantity > 0
        )

    );

  };


  // =====================================================
  // REMOVE FROM CART
  // =====================================================

  const removeFromCart = (productId) => {

    setCart(

      cart.filter(
        (item) =>
          item.id !== productId
      )

    );

  };


  // =====================================================
  // CART TOTAL
  // =====================================================

  const total = cart.reduce(

    (sum, item) =>
      sum +
      Number(item.price) *
      item.quantity,

    0

  );


  // =====================================================
  // TOTAL ITEM COUNT
  // =====================================================

  const totalItems = cart.reduce(

    (sum, item) =>
      sum + item.quantity,

    0

  );


  // =====================================================
  // OPEN CHECKOUT
  // =====================================================

  const openCheckout = () => {

    if (cart.length === 0) {
      return;
    }

    setErrorMessage("");

    setShowCheckout(true);

  };


  // =====================================================
  // CLOSE CHECKOUT
  // =====================================================

  const closeCheckout = () => {

    if (isSubmitting) {
      return;
    }

    setShowCheckout(false);

    setErrorMessage("");

  };


  // =====================================================
  // SUBMIT ORDER
  // =====================================================

  const submitOrder = async (event) => {

    event.preventDefault();


    setErrorMessage("");

    setOrderResult(null);


    // -----------------------------------------------------
    // FRONTEND VALIDATION
    // -----------------------------------------------------

    if (
      !guestName.trim() ||
      !guestEmail.trim() ||
      !guestPhone.trim()
    ) {

      setErrorMessage(
        "Please complete all customer information."
      );

      return;

    }


    if (cart.length === 0) {

      setErrorMessage(
        "Your cart is empty."
      );

      return;

    }


    // -----------------------------------------------------
    // CREATE REQUEST
    // -----------------------------------------------------

    const requestBody = {

      guestName:
        guestName.trim(),

      guestEmail:
        guestEmail.trim(),

      guestPhone:
        guestPhone.trim(),

      paymentMethod:
        paymentMethod,

      items:
        cart.map((item) => ({

          productId:
            item.id,

          quantity:
            item.quantity

        }))

    };


    console.log(
      "Sending order through API Gateway:",
      requestBody
    );


    // -----------------------------------------------------
    // SEND REQUEST TO API GATEWAY
    // -----------------------------------------------------

    try {

      setIsSubmitting(true);


      const response =
        await fetch(

          `${API_BASE_URL}/api/orders`,

          {

            method: "POST",

            headers: {

              "Content-Type":
                "application/json"

            },

            body:
              JSON.stringify(
                requestBody
              )

          }

        );


      // ---------------------------------------------------
      // READ RESPONSE
      // ---------------------------------------------------

      const responseBody =
        await response.json();


      // ---------------------------------------------------
      // HTTP ERROR
      // ---------------------------------------------------

      if (!response.ok) {

        console.error(
          "Order API error:",
          responseBody
        );


        throw new Error(

          responseBody.message ||

          "Order could not be created."

        );

      }


      // ---------------------------------------------------
      // SUCCESS
      // ---------------------------------------------------

      console.log(
        "Order created:",
        responseBody
      );


      setOrderResult(
        responseBody
      );


      // Clear shopping cart
      setCart([]);


      // Close checkout modal
      setShowCheckout(false);


      // Clear form
      setGuestName("");

      setGuestEmail("");

      setGuestPhone("");

      setPaymentMethod(
        "STRIPE"
      );


    } catch (error) {

      console.error(
        "Error creating order:",
        error
      );


      setErrorMessage(

        error.message ||

        "Unable to connect to the API Gateway."

      );


    } finally {

      setIsSubmitting(false);

    }

  };


  // =====================================================
  // UI
  // =====================================================

  return (

    <div className="app">


      {/* =================================================
          HEADER
          ================================================= */}

      <header className="header">

        <div className="header-content">


          <div className="brand">

            <div className="brand-icon">
              M
            </div>


            <div>

              <h1>
                MicroBite
              </h1>

              <p>
                Microservices Restaurant
              </p>

            </div>

          </div>


          <div className="cart-indicator">

            <span className="cart-icon">
              🛒
            </span>


            <div>

              <span className="cart-label">
                Your Cart
              </span>

              <strong>
                {totalItems} items
              </strong>

            </div>

          </div>


        </div>

      </header>


      {/* =================================================
          HERO
          ================================================= */}

      <section className="hero">

        <div className="hero-content">


          <span className="hero-badge">
            Fresh • Fast • Distributed
          </span>


          <h2>

            Delicious food,

            <span>
              {" "}
              powered by microservices.
            </span>

          </h2>


          <p>

            Choose your favorites and build
            your order. Our cloud-native
            backend handles the rest.

          </p>


          <a
            href="#menu"
            className="hero-button"
          >

            Explore Menu

          </a>


        </div>

      </section>


      {/* =================================================
          ORDER SUCCESS
          ================================================= */}

      {orderResult && (

        <section className="order-success">


          <div className="success-icon">
            ✓
          </div>


          <div>

            <span>
              ORDER CREATED
            </span>


            <h3>

              Thank you! Your order
              has been received.

            </h3>


            <p>

              Order ID:

              <strong>
                {" #"}
                {orderResult.id}
              </strong>

              {" • "}

              Total:

              <strong>

                {" $"}

                {Number(
                  orderResult.totalAmount
                ).toFixed(2)}

              </strong>

            </p>

          </div>


          <button
            onClick={() =>
              setOrderResult(null)
            }
          >

            ×

          </button>


        </section>

      )}


      {/* =================================================
          MAIN
          ================================================= */}

      <main className="main-container">


        {/* ===============================================
            PRODUCTS
            =============================================== */}

        <section
          className="products-section"
          id="menu"
        >


          <div className="section-heading">


            <div>

              <span className="section-label">
                OUR MENU
              </span>


              <h2>
                Popular Products
              </h2>

            </div>


            <p>

              {products.length}

              {" "}

              products available

            </p>


          </div>


          {/* PRODUCT LOAD ERROR */}

          {productError && (

            <div className="checkout-error">

              <strong>
                Product Service Error
              </strong>

              <span>
                {productError}
              </span>

            </div>

          )}


          <div className="product-grid">


            {products.map(
              (product) => (

                <article
                  className="product-card"
                  key={product.id}
                >


                  <div className="product-top">


                    <div className="product-number">

                      {String(
                        product.id
                      ).padStart(
                        2,
                        "0"
                      )}

                    </div>


                    <span
                      className={
                        product.active
                          ? "status active"
                          : "status inactive"
                      }
                    >

                      {
                        product.active
                          ? "Available"
                          : "Unavailable"
                      }

                    </span>


                  </div>


                  <div className="product-content">


                    <h3>
                      {product.name}
                    </h3>


                    <p>
                      {product.description}
                    </p>


                  </div>


                  <div className="product-footer">


                    <div className="price">

                      <span>
                        $
                      </span>


                      {Number(
                        product.price
                      ).toFixed(2)}

                    </div>


                    <button
                      className="add-button"
                      onClick={() =>
                        addToCart(
                          product
                        )
                      }
                      disabled={
                        !product.active
                      }
                    >

                      <span>
                        +
                      </span>

                      Add to Cart

                    </button>


                  </div>


                </article>

              )
            )}


          </div>


        </section>


        {/* ===============================================
            CART
            =============================================== */}

        <aside className="cart-section">


          <div className="cart-header">


            <div>

              <span className="section-label">
                YOUR ORDER
              </span>


              <h2>
                Shopping Cart
              </h2>

            </div>


            <span className="cart-count">
              {totalItems}
            </span>


          </div>


          {cart.length === 0 ? (


            <div className="empty-cart">


              <div className="empty-cart-icon">
                🛒
              </div>


              <h3>
                Your cart is empty
              </h3>


              <p>

                Add something delicious
                from the menu to get
                started.

              </p>


            </div>


          ) : (


            <>


              <div className="cart-items">


                {cart.map(
                  (item) => (

                    <div
                      className="cart-item"
                      key={item.id}
                    >


                      <div className="cart-item-info">


                        <h3>
                          {item.name}
                        </h3>


                        <span>

                          $

                          {Number(
                            item.price
                          ).toFixed(2)}

                          {" "}

                          each

                        </span>


                      </div>


                      <div className="cart-item-actions">


                        <div className="quantity-control">


                          <button
                            onClick={() =>
                              decreaseQuantity(
                                item.id
                              )
                            }
                          >

                            −

                          </button>


                          <span>
                            {item.quantity}
                          </span>


                          <button
                            onClick={() =>
                              addToCart(
                                item
                              )
                            }
                          >

                            +

                          </button>


                        </div>


                        <strong className="item-subtotal">

                          $

                          {(
                            Number(
                              item.price
                            ) *
                            item.quantity
                          ).toFixed(2)}

                        </strong>


                        <button
                          className="remove-button"
                          onClick={() =>
                            removeFromCart(
                              item.id
                            )
                          }
                          title="Remove item"
                        >

                          ×

                        </button>


                      </div>


                    </div>

                  )
                )}


              </div>


              <div className="cart-summary">


                <div className="summary-row">

                  <span>
                    Items
                  </span>

                  <span>
                    {totalItems}
                  </span>

                </div>


                <div className="summary-row">

                  <span>
                    Subtotal
                  </span>

                  <span>

                    $

                    {total.toFixed(2)}

                  </span>

                </div>


                <div className="summary-divider" />


                <div className="summary-total">

                  <span>
                    Total
                  </span>


                  <strong>

                    $

                    {total.toFixed(2)}

                  </strong>

                </div>


                <button
                  className="checkout-button"
                  onClick={
                    openCheckout
                  }
                >

                  Continue to Checkout

                  <span>
                    →
                  </span>

                </button>


                <p className="secure-text">
                  🔒 Secure checkout
                </p>


              </div>


            </>

          )}


        </aside>


      </main>


      {/* =================================================
          CHECKOUT MODAL
          ================================================= */}

      {showCheckout && (

        <div className="modal-overlay">


          <div className="checkout-modal">


            <div className="checkout-modal-header">


              <div>

                <span className="section-label">
                  CHECKOUT
                </span>


                <h2>
                  Complete Your Order
                </h2>

              </div>


              <button
                className="modal-close"
                onClick={
                  closeCheckout
                }
              >

                ×

              </button>


            </div>


            <form
              onSubmit={
                submitOrder
              }
            >


              {/* CUSTOMER INFORMATION */}

              <div className="checkout-block">


                <div className="checkout-block-title">


                  <span className="step-number">
                    1
                  </span>


                  <div>

                    <h3>
                      Customer Information
                    </h3>


                    <p>

                      Where should we send
                      your order updates?

                    </p>

                  </div>


                </div>


                <div className="form-group">


                  <label htmlFor="guestName">
                    Full Name
                  </label>


                  <input
                    id="guestName"
                    type="text"
                    placeholder="John Smith"
                    value={
                      guestName
                    }
                    onChange={
                      (event) =>
                        setGuestName(
                          event.target.value
                        )
                    }
                    required
                  />


                </div>


                <div className="form-row">


                  <div className="form-group">


                    <label htmlFor="guestEmail">
                      Email
                    </label>


                    <input
                      id="guestEmail"
                      type="email"
                      placeholder="john@example.com"
                      value={
                        guestEmail
                      }
                      onChange={
                        (event) =>
                          setGuestEmail(
                            event.target.value
                          )
                      }
                      required
                    />


                  </div>


                  <div className="form-group">


                    <label htmlFor="guestPhone">
                      Phone
                    </label>


                    <input
                      id="guestPhone"
                      type="tel"
                      placeholder="5185551234"
                      value={
                        guestPhone
                      }
                      onChange={
                        (event) =>
                          setGuestPhone(
                            event.target.value
                          )
                      }
                      required
                    />


                  </div>


                </div>


              </div>


              {/* PAYMENT METHOD */}

              <div className="checkout-block">


                <div className="checkout-block-title">


                  <span className="step-number">
                    2
                  </span>


                  <div>

                    <h3>
                      Payment Method
                    </h3>


                    <p>
                      Choose how you want
                      to pay.
                    </p>

                  </div>


                </div>


                <div className="payment-options">


                  <label
                    className={
                      paymentMethod ===
                      "STRIPE"
                        ? "payment-option selected"
                        : "payment-option"
                    }
                  >


                    <input
                      type="radio"
                      name="paymentMethod"
                      value="STRIPE"
                      checked={
                        paymentMethod ===
                        "STRIPE"
                      }
                      onChange={
                        (event) =>
                          setPaymentMethod(
                            event.target.value
                          )
                      }
                    />


                    <div className="payment-logo stripe-logo">
                      S
                    </div>


                    <div>

                      <strong>
                        Stripe
                      </strong>


                      <span>
                        Credit or debit card
                      </span>

                    </div>


                    <span className="radio-circle" />


                  </label>


                  <label
                    className={
                      paymentMethod ===
                      "PAYPAL"
                        ? "payment-option selected"
                        : "payment-option"
                    }
                  >


                    <input
                      type="radio"
                      name="paymentMethod"
                      value="PAYPAL"
                      checked={
                        paymentMethod ===
                        "PAYPAL"
                      }
                      onChange={
                        (event) =>
                          setPaymentMethod(
                            event.target.value
                          )
                      }
                    />


                    <div className="payment-logo paypal-logo">
                      P
                    </div>


                    <div>

                      <strong>
                        PayPal
                      </strong>


                      <span>
                        Pay with PayPal
                      </span>

                    </div>


                    <span className="radio-circle" />


                  </label>


                </div>


              </div>


              {/* ORDER REVIEW */}

              <div className="checkout-block">


                <div className="checkout-block-title">


                  <span className="step-number">
                    3
                  </span>


                  <div>

                    <h3>
                      Order Review
                    </h3>


                    <p>

                      Confirm your items
                      before placing the order.

                    </p>

                  </div>


                </div>


                <div className="checkout-items">


                  {cart.map(
                    (item) => (

                      <div
                        className="checkout-item"
                        key={item.id}
                      >


                        <div>

                          <strong>
                            {item.name}
                          </strong>


                          <span>

                            {item.quantity}

                            {" × $"}

                            {Number(
                              item.price
                            ).toFixed(2)}

                          </span>

                        </div>


                        <strong>

                          $

                          {(
                            Number(
                              item.price
                            ) *
                            item.quantity
                          ).toFixed(2)}

                        </strong>


                      </div>

                    )
                  )}


                </div>


                <div className="checkout-total">


                  <span>
                    Total
                  </span>


                  <strong>

                    $

                    {total.toFixed(2)}

                  </strong>


                </div>


              </div>


              {/* ERROR */}

              {errorMessage && (

                <div className="checkout-error">


                  <strong>
                    Order could not be submitted.
                  </strong>


                  <span>
                    {errorMessage}
                  </span>


                </div>

              )}


              {/* SUBMIT */}

              <button
                className="place-order-button"
                type="submit"
                disabled={
                  isSubmitting
                }
              >


                {
                  isSubmitting
                    ? "Processing..."
                    : "Place Order"
                }


                {!isSubmitting && (

                  <span>

                    $

                    {total.toFixed(2)}

                  </span>

                )}


              </button>


              <p className="checkout-security">

                🔒 Your order is processed
                securely by our payment service.

              </p>


            </form>


          </div>


        </div>

      )}


      {/* =================================================
          FOOTER
          ================================================= */}

      <footer className="footer">


        <p>

          MicroBite

          <span>
            •
          </span>

          React + API Gateway + Spring Boot + Kafka

        </p>


      </footer>


    </div>

  );

}


export default App;