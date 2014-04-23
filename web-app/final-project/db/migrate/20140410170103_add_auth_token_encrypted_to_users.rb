class AddAuthTokenEncryptedToUsers < ActiveRecord::Migration
  def change
    add_column :users, :auth_token_encrypted, :string
  end
end
