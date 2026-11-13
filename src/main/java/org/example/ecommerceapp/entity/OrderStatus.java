
    package org.example.ecommerceapp.entity;

    public enum OrderStatus {

        PENDING,
        CONFIRMED,
        SHIPPED,
        DELIVERED,
        CANCELLED;

        public boolean canTransitionTo(OrderStatus newStatus) {

            if (newStatus == null) {
                return false;
            }

            return switch (this) {

                case PENDING ->
                        newStatus == CONFIRMED ||
                                newStatus == CANCELLED;

                case CONFIRMED ->
                        newStatus == SHIPPED ||
                                newStatus == CANCELLED;

                case SHIPPED ->
                        newStatus == DELIVERED;

                case DELIVERED, CANCELLED -> false;
            };
        }

        public boolean isTerminal() {
            return this == DELIVERED || this == CANCELLED;
        }
    }
