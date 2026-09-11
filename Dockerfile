FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN javac --add-modules jdk.httpserver \
    Customer.java \
    MenuItem.java \
    Order.java \
    OrderManager.java \
    DiscountCode.java \
    OutOfStockException.java \
    RestaurantServer.java

CMD ["java", "--add-modules", "jdk.httpserver", "RestaurantServer"]
