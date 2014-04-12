require 'test_helper'

class StolenSessionsControllerTest < ActionController::TestCase
  setup do
    @stolen_session = stolen_sessions(:one)
  end

  test "should get index" do
    get :index
    assert_response :success
    assert_not_nil assigns(:stolen_sessions)
  end

  test "should get new" do
    get :new
    assert_response :success
  end

  test "should create stolen_session" do
    assert_difference('StolenSession.count') do
      post :create, stolen_session: {  }
    end

    assert_redirected_to stolen_session_path(assigns(:stolen_session))
  end

  test "should show stolen_session" do
    get :show, id: @stolen_session
    assert_response :success
  end

  test "should get edit" do
    get :edit, id: @stolen_session
    assert_response :success
  end

  test "should update stolen_session" do
    patch :update, id: @stolen_session, stolen_session: {  }
    assert_redirected_to stolen_session_path(assigns(:stolen_session))
  end

  test "should destroy stolen_session" do
    assert_difference('StolenSession.count', -1) do
      delete :destroy, id: @stolen_session
    end

    assert_redirected_to stolen_sessions_path
  end
end
