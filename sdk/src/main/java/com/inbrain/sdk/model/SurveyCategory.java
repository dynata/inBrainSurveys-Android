package com.inbrain.sdk.model;

public enum SurveyCategory {
    Automotive(1),
    BeveragesAlcoholic(2),
    BeveragesNonAlcoholic(3),
    Business(4),
    ChildrenAndParenting(5),
    CoalitionLoyaltyPrograms(6),
    DestinationsAndTourism(7),
    Education(8),
    ElectronicsComputerSoftware(9),
    EntertainmentAndLeisure(10),
    FinanceBankingInvestingAndInsurance(11),
    Food(12),
    GamblingLottery(13),
    GovernmentAndPolitics(14),
    HealthCare(15),
    Home(16),
    MediaAndPublishing(17),
    PersonalCare(18),
    Restaurants(19),
    SensitiveExplicitContent(20),
    SmokingTobacco(21),
    SocialResearch(22),
    SportsRecreationFitness(23),
    Telecommunications(24),
    Transportation(25),
    TravelAirlines(26),
    TravelHotels(27),
    TravelServicesAgencyBooking(28),
    CreditCards(29),
    VideoGames(30),
    FashionAndClothingOther(31),
    FashionAndClothingDepartmentStore(32),
    ;

    private final int id;

    SurveyCategory(int id) {
        this.id = id;
    }

    public static SurveyCategory fromId(int id) {
        for (SurveyCategory category : values()) {
            if (category.id == id) {
                return category;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }
}