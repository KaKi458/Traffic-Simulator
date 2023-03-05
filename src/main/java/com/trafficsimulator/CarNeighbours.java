package com.trafficsimulator;

public class CarNeighbours {

    private final Car front;
    private final Car back;
    private final Car leftFront;
    private final Car leftBack;
    private final Car rightFront;
    private final Car rightBack;

    public Car getFront() {
        return front;
    }

    public Car getBack() {
        return back;
    }

    public Car getLeftFront() {
        return leftFront;
    }

    public Car getLeftBack() {
        return leftBack;
    }

    public Car getRightFront() {
        return rightFront;
    }

    public Car getRightBack() {
        return rightBack;
    }

    private CarNeighbours(Car front, Car back, Car leftFront, Car leftBack, Car rightFront, Car rightBack) {
        this.front = front;
        this.back = back;
        this.leftFront = leftFront;
        this.leftBack = leftBack;
        this.rightFront = rightFront;
        this.rightBack = rightBack;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Car front, back, leftFront, leftBack, rightFront, rightBack;

        public Builder front(Car front) {
            this.front = front;
            return this;
        }

        public Builder back(Car back) {
            this.back = back;
            return this;
        }

        public Builder leftFront(Car leftFront) {
            this.leftFront = leftFront;
            return this;
        }

        public Builder leftBack(Car leftBack) {
            this.leftBack = leftBack;
            return this;
        }

        public Builder rightFront(Car rightFront) {
            this.rightFront = rightFront;
            return this;
        }

        public Builder rightBack(Car rightBack) {
            this.rightBack = rightBack;
            return this;
        }

        public CarNeighbours build() {
            return new CarNeighbours(
                    front, back, leftFront, leftBack, rightFront, rightBack);
        }
    }
}
