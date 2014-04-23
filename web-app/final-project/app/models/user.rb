class User < ActiveRecord::Base
  def to_param
    handle
  end

  attr_encrypted :auth_secret, :key => 'a secret key', :attribute => 'auth_token_encrypted'

  before_save {
    if(!self.email.nil?)
      self.email = email.downcase 
    end
    self.pending_email = pending_email.downcase 
    self.handle = handle.downcase
  }

  has_many :sessions, dependent: :destroy

  VALID_EMAIL_REGEX = /\A[\w+\-.]+@[a-z\d\-.]+\.[a-z]+\z/i
  validates :pending_email, presence: true, format: { with: VALID_EMAIL_REGEX }, uniqueness: true
  validates :name, presence: true, length: { maximum: 50}

  HANDLE_REGEX = /\A[a-zA-Z]+\z/
  validates :handle, presence: true, length: { maximum: 15}, uniqueness: true, format: { with: HANDLE_REGEX }

  def assign_auth_secret
    self.auth_secret = EmailAuth.generateTOTPSecret
    self.save!
    return self.auth_secret
  end
end
