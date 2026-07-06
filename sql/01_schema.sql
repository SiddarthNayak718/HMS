
BEGIN
  FOR t IN (SELECT table_name FROM user_tables) LOOP
    EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS';
  END LOOP;
END;
/

BEGIN
  FOR s IN (SELECT sequence_name FROM user_sequences) LOOP
    EXECUTE IMMEDIATE 'DROP SEQUENCE ' || s.sequence_name;
  END LOOP;
END;
/


CREATE SEQUENCE seq_patient     START WITH 1001 INCREMENT BY 1;
CREATE SEQUENCE seq_doctor      START WITH 2001 INCREMENT BY 1;
CREATE SEQUENCE seq_dept        START WITH 3001 INCREMENT BY 1;
CREATE SEQUENCE seq_appt        START WITH 4001 INCREMENT BY 1;
CREATE SEQUENCE seq_visit       START WITH 5001 INCREMENT BY 1;
CREATE SEQUENCE seq_presc       START WITH 6001 INCREMENT BY 1;
CREATE SEQUENCE seq_presc_item  START WITH 7001 INCREMENT BY 1;
CREATE SEQUENCE seq_medicine    START WITH 8001 INCREMENT BY 1;
CREATE SEQUENCE seq_labtest     START WITH 9001 INCREMENT BY 1;
CREATE SEQUENCE seq_testorder   START WITH 10001 INCREMENT BY 1;
CREATE SEQUENCE seq_bill        START WITH 11001 INCREMENT BY 1;
CREATE SEQUENCE seq_payment     START WITH 12001 INCREMENT BY 1;

CREATE TABLE Departments (
    dept_id       NUMBER PRIMARY KEY,
    dept_name     VARCHAR2(100) NOT NULL UNIQUE,
    description   VARCHAR2(300),
    location      VARCHAR2(100),
    created_at    DATE DEFAULT SYSDATE
);

CREATE TABLE Doctors (
    doctor_id     NUMBER PRIMARY KEY,
    first_name    VARCHAR2(60) NOT NULL,
    last_name     VARCHAR2(60) NOT NULL,
    specialization VARCHAR2(100),
    dept_id       NUMBER REFERENCES Departments(dept_id),
    phone         VARCHAR2(15) UNIQUE,
    email         VARCHAR2(100) UNIQUE,
    qualification VARCHAR2(200),
    joining_date  DATE DEFAULT SYSDATE,
    status        VARCHAR2(10) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE'))
);


CREATE TABLE Patients (
    patient_id    NUMBER PRIMARY KEY,
    first_name    VARCHAR2(60) NOT NULL,
    last_name     VARCHAR2(60) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender        VARCHAR2(10) CHECK (gender IN ('MALE','FEMALE','OTHER')),
    blood_group   VARCHAR2(5),
    phone         VARCHAR2(15) NOT NULL UNIQUE,
    email         VARCHAR2(100),
    address       VARCHAR2(300),
    emergency_contact VARCHAR2(100),
    registered_at DATE DEFAULT SYSDATE,
    status        VARCHAR2(10) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE'))
);

CREATE TABLE Appointments (
    appt_id       NUMBER PRIMARY KEY,
    patient_id    NUMBER NOT NULL REFERENCES Patients(patient_id),
    doctor_id     NUMBER NOT NULL REFERENCES Doctors(doctor_id),
    appt_date     DATE NOT NULL,
    appt_time     VARCHAR2(10) NOT NULL,
    reason        VARCHAR2(300),
    status        VARCHAR2(15) DEFAULT 'SCHEDULED'
                    CHECK (status IN ('SCHEDULED','COMPLETED','CANCELLED','NO_SHOW')),
    created_at    DATE DEFAULT SYSDATE,
    CONSTRAINT uq_appt UNIQUE (doctor_id, appt_date, appt_time)
);

CREATE TABLE Visits (
    visit_id      NUMBER PRIMARY KEY,
    appt_id       NUMBER REFERENCES Appointments(appt_id),
    patient_id    NUMBER NOT NULL REFERENCES Patients(patient_id),
    doctor_id     NUMBER NOT NULL REFERENCES Doctors(doctor_id),
    visit_date    DATE DEFAULT SYSDATE,
    diagnosis     VARCHAR2(500),
    notes         VARCHAR2(1000),
    follow_up_date DATE
);


CREATE TABLE Medicines (
    medicine_id   NUMBER PRIMARY KEY,
    name          VARCHAR2(150) NOT NULL UNIQUE,
    category      VARCHAR2(80),
    unit_price    NUMBER(10,2) NOT NULL CHECK (unit_price >= 0),
    stock_qty     NUMBER(10) DEFAULT 0 CHECK (stock_qty >= 0),
    manufacturer  VARCHAR2(150),
    expiry_date   DATE
);


CREATE TABLE Prescriptions (
    presc_id      NUMBER PRIMARY KEY,
    visit_id      NUMBER NOT NULL REFERENCES Visits(visit_id),
    patient_id    NUMBER NOT NULL REFERENCES Patients(patient_id),
    doctor_id     NUMBER NOT NULL REFERENCES Doctors(doctor_id),
    presc_date    DATE DEFAULT SYSDATE,
    notes         VARCHAR2(500)
);


CREATE TABLE Prescription_Items (
    item_id       NUMBER PRIMARY KEY,
    presc_id      NUMBER NOT NULL REFERENCES Prescriptions(presc_id),
    medicine_id   NUMBER NOT NULL REFERENCES Medicines(medicine_id),
    dosage        VARCHAR2(100),
    frequency     VARCHAR2(50),
    duration_days NUMBER(3),
    quantity      NUMBER(5) NOT NULL CHECK (quantity > 0)
);


CREATE TABLE Lab_Tests (
    test_id       NUMBER PRIMARY KEY,
    test_name     VARCHAR2(150) NOT NULL UNIQUE,
    description   VARCHAR2(300),
    price         NUMBER(10,2) NOT NULL CHECK (price >= 0),
    normal_range  VARCHAR2(100)
);


CREATE TABLE Test_Orders (
    order_id      NUMBER PRIMARY KEY,
    visit_id      NUMBER NOT NULL REFERENCES Visits(visit_id),
    patient_id    NUMBER NOT NULL REFERENCES Patients(patient_id),
    test_id       NUMBER NOT NULL REFERENCES Lab_Tests(test_id),
    ordered_by    NUMBER NOT NULL REFERENCES Doctors(doctor_id),
    order_date    DATE DEFAULT SYSDATE,
    result_value  VARCHAR2(300),
    result_date   DATE,
    status        VARCHAR2(15) DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING','COMPLETED','CANCELLED'))
);


CREATE TABLE Bills (
    bill_id         NUMBER PRIMARY KEY,
    patient_id      NUMBER NOT NULL REFERENCES Patients(patient_id),
    visit_id        NUMBER REFERENCES Visits(visit_id),
    bill_date       DATE DEFAULT SYSDATE,
    consultation_fee NUMBER(10,2) DEFAULT 0,
    medicine_amount  NUMBER(10,2) DEFAULT 0,
    lab_amount       NUMBER(10,2) DEFAULT 0,
    total_amount     NUMBER(10,2) DEFAULT 0,
    discount         NUMBER(5,2) DEFAULT 0,
    net_amount       NUMBER(10,2) DEFAULT 0,
    status           VARCHAR2(10) DEFAULT 'UNPAID' CHECK (status IN ('UNPAID','PARTIAL','PAID'))
);


CREATE TABLE Payments (
    payment_id    NUMBER PRIMARY KEY,
    bill_id       NUMBER NOT NULL REFERENCES Bills(bill_id),
    amount_paid   NUMBER(10,2) NOT NULL CHECK (amount_paid > 0),
    payment_date  DATE DEFAULT SYSDATE,
    payment_mode  VARCHAR2(20) CHECK (payment_mode IN ('CASH','CARD','UPI','INSURANCE','ONLINE')),
    remarks       VARCHAR2(200)
);


CREATE INDEX idx_appt_date     ON Appointments(appt_date);
CREATE INDEX idx_appt_patient  ON Appointments(patient_id);
CREATE INDEX idx_visit_patient ON Visits(patient_id);
CREATE INDEX idx_bill_patient  ON Bills(patient_id);
CREATE INDEX idx_patient_phone ON Patients(phone);


CREATE OR REPLACE TRIGGER trg_patient_pk
BEFORE INSERT ON Patients FOR EACH ROW
BEGIN IF :NEW.patient_id IS NULL THEN SELECT seq_patient.NEXTVAL INTO :NEW.patient_id FROM DUAL; END IF; END;
/
CREATE OR REPLACE TRIGGER trg_doctor_pk
BEFORE INSERT ON Doctors FOR EACH ROW
BEGIN IF :NEW.doctor_id IS NULL THEN SELECT seq_doctor.NEXTVAL INTO :NEW.doctor_id FROM DUAL; END IF; END;
/
CREATE OR REPLACE TRIGGER trg_dept_pk
BEFORE INSERT ON Departments FOR EACH ROW
BEGIN IF :NEW.dept_id IS NULL THEN SELECT seq_dept.NEXTVAL INTO :NEW.dept_id FROM DUAL; END IF; END;
/
CREATE OR REPLACE TRIGGER trg_appt_pk
BEFORE INSERT ON Appointments FOR EACH ROW
BEGIN IF :NEW.appt_id IS NULL THEN SELECT seq_appt.NEXTVAL INTO :NEW.appt_id FROM DUAL; END IF; END;
/
CREATE OR REPLACE TRIGGER trg_visit_pk
BEFORE INSERT ON Visits FOR EACH ROW
BEGIN IF :NEW.visit_id IS NULL THEN SELECT seq_visit.NEXTVAL INTO :NEW.visit_id FROM DUAL; END IF; END;
/
CREATE OR REPLACE TRIGGER trg_presc_pk
BEFORE INSERT ON Prescriptions FOR EACH ROW
BEGIN IF :NEW.presc_id IS NULL THEN SELECT seq_presc.NEXTVAL INTO :NEW.presc_id FROM DUAL; END IF; END;
/
CREATE OR REPLACE TRIGGER trg_presc_item_pk
BEFORE INSERT ON Prescription_Items FOR EACH ROW
BEGIN IF :NEW.item_id IS NULL THEN SELECT seq_presc_item.NEXTVAL INTO :NEW.item_id FROM DUAL; END IF; END;
/
CREATE OR REPLACE TRIGGER trg_medicine_pk
BEFORE INSERT ON Medicines FOR EACH ROW
BEGIN IF :NEW.medicine_id IS NULL THEN SELECT seq_medicine.NEXTVAL INTO :NEW.medicine_id FROM DUAL; END IF; END;
/
CREATE OR REPLACE TRIGGER trg_labtest_pk
BEFORE INSERT ON Lab_Tests FOR EACH ROW
BEGIN IF :NEW.test_id IS NULL THEN SELECT seq_labtest.NEXTVAL INTO :NEW.test_id FROM DUAL; END IF; END;
/
CREATE OR REPLACE TRIGGER trg_testorder_pk
BEFORE INSERT ON Test_Orders FOR EACH ROW
BEGIN IF :NEW.order_id IS NULL THEN SELECT seq_testorder.NEXTVAL INTO :NEW.order_id FROM DUAL; END IF; END;
/
CREATE OR REPLACE TRIGGER trg_bill_pk
BEFORE INSERT ON Bills FOR EACH ROW
BEGIN IF :NEW.bill_id IS NULL THEN SELECT seq_bill.NEXTVAL INTO :NEW.bill_id FROM DUAL; END IF; END;
/
CREATE OR REPLACE TRIGGER trg_payment_pk
BEFORE INSERT ON Payments FOR EACH ROW
BEGIN IF :NEW.payment_id IS NULL THEN SELECT seq_payment.NEXTVAL INTO :NEW.payment_id FROM DUAL; END IF; END;
/

CREATE OR REPLACE TRIGGER trg_update_medicine_stock
AFTER INSERT ON Prescription_Items FOR EACH ROW
BEGIN
    UPDATE Medicines SET stock_qty = stock_qty - :NEW.quantity
    WHERE medicine_id = :NEW.medicine_id;
END;
/

CREATE OR REPLACE TRIGGER trg_calc_bill_total
BEFORE INSERT OR UPDATE ON Bills FOR EACH ROW
BEGIN
    :NEW.total_amount := NVL(:NEW.consultation_fee,0)
                       + NVL(:NEW.medicine_amount,0)
                       + NVL(:NEW.lab_amount,0);
    :NEW.net_amount   := :NEW.total_amount - NVL(:NEW.discount,0);
END;
/


CREATE OR REPLACE TRIGGER trg_update_bill_status
AFTER INSERT ON Payments FOR EACH ROW
DECLARE
    v_net    NUMBER;
    v_paid   NUMBER;
BEGIN
    SELECT net_amount INTO v_net FROM Bills WHERE bill_id = :NEW.bill_id;
    SELECT NVL(SUM(amount_paid),0) INTO v_paid FROM Payments WHERE bill_id = :NEW.bill_id;
    IF v_paid >= v_net THEN
        UPDATE Bills SET status = 'PAID' WHERE bill_id = :NEW.bill_id;
    ELSIF v_paid > 0 THEN
        UPDATE Bills SET status = 'PARTIAL' WHERE bill_id = :NEW.bill_id;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_appt_on_visit
AFTER INSERT ON Visits FOR EACH ROW
BEGIN
    IF :NEW.appt_id IS NOT NULL THEN
        UPDATE Appointments SET status = 'COMPLETED' WHERE appt_id = :NEW.appt_id;
    END IF;
END;
/


CREATE OR REPLACE PROCEDURE proc_generate_bill(
    p_patient_id  IN NUMBER,
    p_visit_id    IN NUMBER,
    p_consult_fee IN NUMBER,
    p_discount    IN NUMBER DEFAULT 0,
    p_bill_id     OUT NUMBER
) AS
    v_med_amt  NUMBER := 0;
    v_lab_amt  NUMBER := 0;
BEGIN

    SELECT NVL(SUM(pi.quantity * m.unit_price), 0)
    INTO v_med_amt
    FROM Prescription_Items pi
    JOIN Prescriptions pr ON pr.presc_id = pi.presc_id
    JOIN Medicines m ON m.medicine_id = pi.medicine_id
    WHERE pr.visit_id = p_visit_id;

    SELECT NVL(SUM(lt.price), 0)
    INTO v_lab_amt
    FROM Test_Orders to2
    JOIN Lab_Tests lt ON lt.test_id = to2.test_id
    WHERE to2.visit_id = p_visit_id AND to2.status != 'CANCELLED';

    SELECT seq_bill.NEXTVAL INTO p_bill_id FROM DUAL;
    INSERT INTO Bills(bill_id, patient_id, visit_id, consultation_fee,
                      medicine_amount, lab_amount, discount)
    VALUES(p_bill_id, p_patient_id, p_visit_id, p_consult_fee,
           v_med_amt, v_lab_amt, p_discount);
    COMMIT;
END;
/


INSERT INTO Departments(dept_name, description, location) VALUES
  ('General Medicine','General outpatient services','Block A');
INSERT INTO Departments(dept_name, description, location) VALUES
  ('Cardiology','Heart and cardiovascular diseases','Block B');
INSERT INTO Departments(dept_name, description, location) VALUES
  ('Orthopedics','Bone and joint care','Block C');
INSERT INTO Departments(dept_name, description, location) VALUES
  ('Pediatrics','Children healthcare','Block A');
INSERT INTO Departments(dept_name, description, location) VALUES
  ('Pathology','Laboratory and diagnostics','Block D');

INSERT INTO Doctors(first_name,last_name,specialization,dept_id,phone,email,qualification)
  VALUES('Rajesh','Kumar','General Physician',3001,'9900001111','rajesh@hms.com','MBBS, MD');
INSERT INTO Doctors(first_name,last_name,specialization,dept_id,phone,email,qualification)
  VALUES('Priya','Sharma','Cardiologist',3002,'9900002222','priya@hms.com','MBBS, DM Cardiology');
INSERT INTO Doctors(first_name,last_name,specialization,dept_id,phone,email,qualification)
  VALUES('Suresh','Nair','Orthopedic Surgeon',3003,'9900003333','suresh@hms.com','MBBS, MS Ortho');
INSERT INTO Doctors(first_name,last_name,specialization,dept_id,phone,email,qualification)
  VALUES('Anita','Patel','Pediatrician',3004,'9900004444','anita@hms.com','MBBS, MD Pediatrics');

INSERT INTO Medicines(name,category,unit_price,stock_qty,manufacturer)
  VALUES('Paracetamol 500mg','Analgesic',5.00,500,'Cipla');
INSERT INTO Medicines(name,category,unit_price,stock_qty,manufacturer)
  VALUES('Amoxicillin 250mg','Antibiotic',12.50,300,'Sun Pharma');
INSERT INTO Medicines(name,category,unit_price,stock_qty,manufacturer)
  VALUES('Omeprazole 20mg','Antacid',8.00,400,'Dr. Reddys');
INSERT INTO Medicines(name,category,unit_price,stock_qty,manufacturer)
  VALUES('Metformin 500mg','Antidiabetic',6.50,600,'Lupin');
INSERT INTO Medicines(name,category,unit_price,stock_qty,manufacturer)
  VALUES('Atenolol 50mg','Beta-Blocker',7.00,350,'Cipla');

INSERT INTO Lab_Tests(test_name,description,price,normal_range)
  VALUES('Complete Blood Count','CBC panel',250.00,'See report');
INSERT INTO Lab_Tests(test_name,description,price,normal_range)
  VALUES('Blood Sugar Fasting','Glucose level fasting',80.00,'70-100 mg/dL');
INSERT INTO Lab_Tests(test_name,description,price,normal_range)
  VALUES('Lipid Profile','Cholesterol panel',350.00,'LDL < 100');
INSERT INTO Lab_Tests(test_name,description,price,normal_range)
  VALUES('ECG','Electrocardiogram',200.00,'Normal sinus rhythm');
INSERT INTO Lab_Tests(test_name,description,price,normal_range)
  VALUES('Urine Routine','Urinalysis',100.00,'Normal');

COMMIT;
