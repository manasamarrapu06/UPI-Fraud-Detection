from flask import Flask, request, jsonify
import joblib
import os
import pandas as pd

app = Flask(__name__)

# Load trained model
MODEL_PATH = os.path.join(
    os.path.dirname(__file__),
    "fraud_model.pkl"
)

model = joblib.load(MODEL_PATH)


@app.route("/", methods=["GET"])
def home():
    return jsonify({
        "message": "UPI Fraud Detection AI Service is running",
        "status": "SUCCESS"
    })


@app.route("/predict", methods=["POST"])
def predict():

    try:
        data = request.get_json()

        amount = float(data.get("amount", 0))
        location_risk = int(data.get("location_risk", 0))
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

        # Create DataFrame with the same feature names
        # used during model training
        features = pd.DataFrame([{
            "amount": amount,
            "location_risk": location_risk,
            "transaction_frequency": transaction_frequency,
            "previous_fraud": previous_fraud,
            "recipient_new": recipient_new,
            "night_transaction": night_transaction
        }])

        # Prediction
        prediction = int(model.predict(features)[0])

        # Fraud probability
        probabilities = model.predict_proba(features)[0]

        fraud_probability = float(probabilities[1])

        # Convert probability to percentage
        risk_score = round(
            fraud_probability * 100,
            2
        )

        # Determine risk level
        if risk_score >= 70:
            risk_level = "HIGH"
        elif risk_score >= 40:
            risk_level = "MEDIUM"
        else:
            risk_level = "LOW"upi-fraud-background.png

        # Determine result
        if prediction == 1:
            result = "FRAUD"
        else:
            result = "SAFE"

        return jsonify({
            "prediction": prediction,
            "result": result,
            "risk_score": risk_score,
            "risk_level": risk_level,
            "amount": amount
        })

    except Exception as e:

        return jsonify({
            "error": str(e)
        }), 400


if __name__ == "__main__":

    app.run(
        host="127.0.0.1",
        port=5000,
        debug=True
    )