import pandas as pd
import joblib

from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier

from sklearn.metrics import (
    accuracy_score,
    precision_score,
    recall_score,
    f1_score,
    confusion_matrix,
    classification_report
)


# ==========================================
# 1. LOAD DATASET
# ==========================================

data = pd.read_csv("dataset/transactions.csv")

print("==========================================")
print("AI-BASED UPI FRAUD DETECTION")
print("==========================================")

print("\nDataset loaded successfully!")
print("Total records:", len(data))

print("\nColumns:")
print(data.columns.tolist())


# ==========================================
# 2. SEPARATE INPUT AND OUTPUT
# ==========================================

X = data.drop("fraud", axis=1)
y = data["fraud"]


# ==========================================
# 3. SPLIT DATASET
# ==========================================

X_train, X_test, y_train, y_test = train_test_split(
    X,
    y,
    test_size=0.2,
    random_state=42,
    stratify=y
)

print("\nTraining records:", len(X_train))
print("Testing records:", len(X_test))


# ==========================================
# 4. CREATE RANDOM FOREST MODEL
# ==========================================

model = RandomForestClassifier(
    n_estimators=100,
    random_state=42
)


# ==========================================
# 5. TRAIN MODEL
# ==========================================

print("\nTraining Random Forest model...")

model.fit(X_train, y_train)

print("Model training completed!")


# ==========================================
# 6. PREDICTION
# ==========================================

y_pred = model.predict(X_test)


# ==========================================
# 7. MODEL EVALUATION
# ==========================================

accuracy = accuracy_score(
    y_test,
    y_pred
)

precision = precision_score(
    y_test,
    y_pred,
    zero_division=0
)

recall = recall_score(
    y_test,
    y_pred,
    zero_division=0
)

f1 = f1_score(
    y_test,
    y_pred,
    zero_division=0
)


print("\n==========================================")
print("MODEL EVALUATION")
print("==========================================")

print(f"Accuracy  : {accuracy:.2f}")
print(f"Precision : {precision:.2f}")
print(f"Recall    : {recall:.2f}")
print(f"F1 Score  : {f1:.2f}")


# ==========================================
# 8. CONFUSION MATRIX
# ==========================================

cm = confusion_matrix(
    y_test,
    y_pred
)

print("\n==========================================")
print("CONFUSION MATRIX")
print("==========================================")

print(cm)


# ==========================================
# 9. CLASSIFICATION REPORT
# ==========================================

print("\n==========================================")
print("CLASSIFICATION REPORT")
print("==========================================")

print(
    classification_report(
        y_test,
        y_pred,
        zero_division=0
    )
)


# ==========================================
# 10. SAVE MODEL
# ==========================================

joblib.dump(
    model,
    "fraud_model.pkl"
)

print("\n==========================================")
print("MODEL SAVED SUCCESSFULLY")
print("==========================================")

print("Created: fraud_model.pkl")