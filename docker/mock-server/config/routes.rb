Rails.application.routes.draw do
  scope :api do
    resources :users, only: [] do
      resources :apps, only: %i[create], module: :users
    end
  end
end
