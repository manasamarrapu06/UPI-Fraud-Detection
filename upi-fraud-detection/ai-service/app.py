from flask import Flask, request, jsonify
import joblib
import os
import pandas as pd

app = Flask(__name__)

# --------------------------------------------------
# Load trained ML model
# --------------------------------------------------

MODEL_PATH = os.path.join(
    os.path.dirname(__file__),
    "fraud_model.pkl"
)

try:
    model = joblib.load(MODEL_PATH)
    print("Fraud detection model loaded successfully.")
except Exception as e:
    model = None
    print("Error loading model:", e)


# --------------------------------------------------
# Home API
# --------------------------------------------------

@app.route("/", methods=["GET"])
def home():

    return jsonify({
        "message": "UPI Fraud Detection AI Service is running",
        "status": "SUCCESS"
    })


# --------------------------------------------------
# Prediction API
# --------------------------------------------------

@app.route("/predict", methods=["POST"])
def predict():

    try:

        # Check whether model is loaded
        if model is None:

            return jsonify({
                "error": "ML model could not be loaded"
            }), 500


        # Get JSON request
        data = request.get_json()

        if data is None:

            return jsonify({
                "error": "Request body must contain JSON data"
            }), 400


        # --------------------------------------------------
        # Read input values
        # --------------------------------------------------

        amount = float(
            data.get("amount", 0)
        )

        location_risk = int(
            data.get("location_risk", 0)
        )

        transaction_frequency = int(
            data.get("transaction_frequency", 0)
        )

        previous_fraud = int(
            data.get("previous_fraud", 0)
        )

        recipient_new = int(
            data.get("recipient_new", 0)
        )

        night_transaction = int(
            data.get("night_transaction", 0)
        )


        # --------------------------------------------------
        # Create DataFrame
        # IMPORTANT:
        # These names must match training columns
        # --------------------------------------------------

        features = pd.DataFrame([{

            "amount": amount,

            "location_risk": location_risk,

            "transaction_frequency":
                transaction_frequency,

            "previous_fraud":
                previous_fraud,

            "recipient_new":
                recipient_new,

            "night_transaction":
                night_transaction

        }])


        # --------------------------------------------------
        # ML Prediction
        # --------------------------------------------------

        prediction = int(
            model.predict(features)[0]
        )


        # --------------------------------------------------
        # Fraud Probability
        # --------------------------------------------------

        probabilities = model.predict_proba(features)[0]


        # Probability of class 1 = fraud
        fraud_probability = float(
            probabilities[1]
        )


        # Convert to percentage
        risk_score = round(
            fraud_probability * 100,
            2
        )


        # --------------------------------------------------
        # Risk Level
        # --------------------------------------------------

        if risk_score >= 70:

            risk_level = "HIGH"

        elif risk_score >= 40:

            risk_level = "MEDIUM"

        else:

            risk_level = "LOW"


        # --------------------------------------------------
        # Result
        # --------------------------------------------------

        if prediction == 1:

            result = "FRAUD"

        else:

            result = "SAFE"


        # --------------------------------------------------
        # Response
        # --------------------------------------------------

        return jsonify({

            "prediction": prediction,

            "result": result,

            "risk_score": risk_score,

            "risk_level": risk_level,

            "amount": amount,

            "location_risk": location_risk,

            "transaction_frequency":
                transaction_frequency,

            "previous_fraud":
                previous_fraud,

            "recipient_new":
                recipient_new,

            "night_transaction":
                night_transaction

        })


    except Exception as e:

        print("Prediction error:", e)

        return jsonify({

            "error": str(e)

        }), 400


# --------------------------------------------------
# Start Flask server
# --------------------------------------------------

if __name__ == "__main__":

    app.run(

        host="127.0.0.1",

        port=5000,

        debug=True

    )